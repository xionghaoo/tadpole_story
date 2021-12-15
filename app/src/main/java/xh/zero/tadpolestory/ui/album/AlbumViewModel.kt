package xh.zero.tadpolestory.ui.album

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.example.android.uamp.common.EMPTY_PLAYBACK_STATE
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.common.NOTHING_PLAYING
import com.example.android.uamp.media.extensions.id
import com.example.android.uamp.media.extensions.isPlayEnabled
import com.example.android.uamp.media.extensions.isPlaying
import com.example.android.uamp.media.extensions.isPrepared
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import xh.zero.core.vo.NetworkState
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.LOAD_SONG_FOR_PAGE
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.ui.MediaItemData

class AlbumViewModel @AssistedInject constructor(
    val repo: Repository,
    private val _musicServiceConnection: MusicServiceConnection,
    @Assisted private val mediaId: String
) : ViewModel() {

    private val _mediaItems = MutableLiveData<List<MediaItemData>>()
    val loadMediaItems = MutableLiveData<List<MediaItemData>>()
    val mediaItems: LiveData<List<MediaItemData>> = _mediaItems
    val networkState = MutableLiveData<NetworkState>()

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            networkState.postValue(NetworkState.LOADED)

            val itemsList = children.map { child ->
                val subtitle = child.description.subtitle ?: ""
                val duration = child.description.extras?.getLong("duration")
                val trackNumber = child.description.extras?.getLong("trackNumber")
                MediaItemData(
                    mediaId = child.mediaId!!,
                    title = child.description.title.toString(),
                    subtitle = subtitle.toString(),
                    albumArtUri = child.description.iconUri,
                    browsable = child.isBrowsable,
                    playbackRes = getResourceForMediaId(child.mediaId!!),
                    duration = duration ?: 0,
                    trackNumber = trackNumber ?: 0L
                )
            }
            val result = itemsList
                .mapIndexed { index, item ->
                    if (index % 2 == 0) {
                        item.extraItem = if (index + 1 < itemsList.size) itemsList[index + 1] else null
                    }
                    item
                }
                .filterIndexed { index, _ -> index % 2 == 0 }

            _mediaItems.postValue(result)
            loadMediaItems.postValue(result)
        }
    }



    /**
     * When the session's [PlaybackStateCompat] changes, the [mediaItems] need to be updated
     * so the correct [MediaItemData.playbackRes] is displayed on the active item.
     * (i.e.: play/pause button or blank)
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        val playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = _musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING
        if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
            _mediaItems.postValue(updateState(playbackState, metadata))
        }
    }

    /**
     * When the session's [MediaMetadataCompat] changes, the [mediaItems] need to be updated
     * as it means the currently active item has changed. As a result, the new, and potentially
     * old item (if there was one), both need to have their [MediaItemData.playbackRes]
     * changed. (i.e.: play/pause button or blank)
     */
    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        val playbackState = _musicServiceConnection.playbackState.value ?: EMPTY_PLAYBACK_STATE
        val metadata = it ?: NOTHING_PLAYING
        if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
            _mediaItems.postValue(updateState(playbackState, metadata))
        }
    }

    private val networkFailureObserver = Observer<Boolean> { failure ->
        if (!failure) networkState.postValue(NetworkState.error("声音列表加载失败"))
    }

    private val musicServiceConnection = _musicServiceConnection.also {
        it.subscribe(mediaId, subscriptionCallback)

        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
        it.networkFailure.observeForever(networkFailureObserver)
    }

    private fun updateState(
        playbackState: PlaybackStateCompat,
        mediaMetadata: MediaMetadataCompat
    ): List<MediaItemData> {

        val newResId = when (playbackState.isPlaying) {
            true -> R.drawable.ic_pause_black_24dp
            else -> R.drawable.ic_play_arrow_black_24dp
        }

        return mediaItems.value?.map {
            val useResId = if (it.mediaId == mediaMetadata.id) newResId else NO_RES
            it.copy(playbackRes = useResId)
        } ?: emptyList()
    }

    private fun getResourceForMediaId(mediaId: String): Int {
        val isActive = mediaId == musicServiceConnection.nowPlaying.value?.id
        val isPlaying = musicServiceConnection.playbackState.value?.isPlaying ?: false
        return when {
            !isActive -> NO_RES
            isPlaying -> R.drawable.ic_pause_black_24dp
            else -> R.drawable.ic_play_arrow_black_24dp
        }
    }

    /**
     * This method takes a [MediaItemData] and does one of the following:
     * - If the item is *not* the active item, then play it directly.
     * - If the item *is* the active item, check whether "pause" is a permitted command. If it is,
     *   then pause playback, otherwise send "play" to resume playback.
     */
    fun playMedia(mediaItem: MediaItemData, pauseAllowed: Boolean = true) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val transportControls = musicServiceConnection.transportControls

        val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == nowPlaying?.id) {
            musicServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying ->
                        if (pauseAllowed) transportControls.pause() else Unit
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Timber.w(
                            "Playable item clicked but neither play nor pause are enabled!" +
                                    " (mediaId=${mediaItem.mediaId})"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    fun loadSongs(page: Int, isRefresh: Boolean, isPaging: Boolean) {
        networkState.postValue(NetworkState.LOADING)
        musicServiceConnection.sendCommand(LOAD_SONG_FOR_PAGE, Bundle().apply {
            putString("mediaId", mediaId)
            putInt("page", page)
            putBoolean("isRefresh", isRefresh)
            putBoolean("isPaging", isPaging)
        }) { code, bundle ->

        }
    }

    fun subscribeAlbum(id: Int) = repo.subscribeAlbum(id)

    /**
     * Since we use [LiveData.observeForever] above (in [musicServiceConnection]), we want
     * to call [LiveData.removeObserver] here to prevent leaking resources when the [ViewModel]
     * is not longer in use.
     *
     * For more details, see the kdoc on [musicServiceConnection] above.
     */
    override fun onCleared() {
        super.onCleared()

        // Remove the permanent observers from the MusicServiceConnection.
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)
        musicServiceConnection.networkFailure.removeObserver(networkFailureObserver)

        // And then, finally, unsubscribe the media ID that was being watched.
        musicServiceConnection.unsubscribe(mediaId, subscriptionCallback)
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(mediaId: String): AlbumViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            mediaId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(mediaId) as T
            }
        }
    }
}

private const val NO_RES = 0