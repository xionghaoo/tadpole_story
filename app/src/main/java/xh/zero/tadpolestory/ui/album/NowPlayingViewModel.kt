package xh.zero.tadpolestory.ui.album

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.android.uamp.common.EMPTY_PLAYBACK_STATE
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.common.NOTHING_PLAYING
import com.example.android.uamp.media.extensions.*
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.*
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    data class NowPlayingMetadata(
        val id: String,
        val albumArtUri: Uri,
        val title: String?,
        val subtitle: String?,
        val duration: Long,
        val orderNum: Long,
        val totalNum: Long
    ) {
        companion object {
            /**
             * Utility method to convert milliseconds to a display of minutes and seconds
             */
            fun timestampToMSS(position: Long): String {
                val totalSeconds = Math.floor(position / 1E3).toInt()
                val minutes = totalSeconds / 60
                val remainingSeconds = totalSeconds - (minutes * 60)
                return if (position < 0) "--:--"
                else "%d:%02d".format(minutes, remainingSeconds)
            }
        }
    }

    private var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE
    val mediaMetadata = MutableLiveData<NowPlayingMetadata>()
    val mediaPosition = MutableLiveData<Long>().apply {
        postValue(0L)
    }
    val mediaProgress = MutableLiveData<Int>().apply {
        postValue(0)
    }
    val mediaButtonRes = MutableLiveData<Int>().apply {
        postValue(R.mipmap.ic_media_play)
    }
    val switchState = MutableLiveData<Pair<Boolean, Boolean>>().apply {
        postValue(Pair(first = false, second = true))
    }

    /**
     * When the session's [PlaybackStateCompat] changes, the [mediaItems] need to be updated
     * so the correct [MediaItemData.playbackRes] is displayed on the active item.
     * (i.e.: play/pause button or blank)
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(playbackState, metadata)
    }

    /**
     * When the session's [MediaMetadataCompat] changes, the [mediaItems] need to be updated
     * as it means the currently active item has changed. As a result, the new, and potentially
     * old item (if there was one), both need to have their [MediaItemData.playbackRes]
     * changed. (i.e.: play/pause button or blank)
     */
    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        updateState(playbackState, it)
    }

    private val musicServiceConnection = musicServiceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
        checkPlaybackPosition()
    }

    /**
     * Internal function that recursively calls itself every [POSITION_UPDATE_INTERVAL_MILLIS] ms
     * to check the current playback position and updates the corresponding LiveData object when it
     * has changed.
     */
    private fun checkPlaybackPosition(): Boolean = handler.postDelayed({
        val currPosition = playbackState.currentPlayBackPosition
        if (mediaPosition.value != currPosition) {
            mediaPosition.postValue(currPosition)
            val totalDuration = mediaMetadata.value?.duration ?: 0L
            mediaProgress.postValue(((currPosition.toFloat() / totalDuration) * 500).roundToInt())
        }
        if (updatePosition)
            checkPlaybackPosition()
    }, POSITION_UPDATE_INTERVAL_MILLIS)

    fun playMediaId(mediaId: String) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val transportControls = musicServiceConnection.transportControls

        val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaId == nowPlaying?.id) {
            musicServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Timber.w(
                            "Playable item clicked but neither play nor pause are enabled!" +
                                    " (mediaId=$mediaId)"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaId, null)
        }
    }

    fun rePlay() {
        musicServiceConnection.playbackState.value?.let { playbackState ->
            if (!playbackState.isPlaying) {
                mediaMetadata.value?.let {
                    playMediaId(it.id)
                }
            }
        }
    }

    fun seekToPosition(posMs: Long, success: () -> Unit) {
        musicServiceConnection.sendCommand(SEEK_TO_POSITION, Bundle().apply {
            putLong(EXTRA_MEDIA_POSITION, posMs)
        }) { code, _ ->
            if (code == Activity.RESULT_OK) {
                success()
            }
        }
    }

    fun next() {
        musicServiceConnection.sendCommand(PLAY_NEXT, Bundle.EMPTY) { code, bundle ->
//            bundle?.getBoolean(HAS_NEXT)?.let {
//                switchState.postValue(Pair(first = true, second = it))
//            }
        }
    }

    fun prev() {
        musicServiceConnection.sendCommand(PLAY_PREV, Bundle.EMPTY) { code, bundle ->
//            bundle?.getBoolean(HAS_PREV)?.let {
//                switchState.postValue(Pair(first = it, second = true))
//            }
        }
    }

    private fun checkSwitchState(orderNum: Long, totalNum: Long) {
        if (orderNum == 0L) {
            switchState.postValue(Pair(first = false, second = true))
        } else if (orderNum == totalNum - 1) {
            switchState.postValue(Pair(first = true, second = false))
        } else {
            switchState.postValue(Pair(first = true, second = true))
        }
    }

    override fun onCleared() {
        super.onCleared()

        // Remove the permanent observers from the MusicServiceConnection.
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)

        // Stop updating the position
        updatePosition = false
    }

    private fun updateState(
        playbackState: PlaybackStateCompat,
        mediaMetadata: MediaMetadataCompat
    ) {
        // Only update media item once we have duration available
        if (mediaMetadata.duration != 0L && mediaMetadata.id != null) {
            val nowPlayingMetadata = NowPlayingMetadata(
                mediaMetadata.id!!,
                mediaMetadata.albumArtUri,
                mediaMetadata.title?.trim(),
                mediaMetadata.displaySubtitle?.trim(),
                mediaMetadata.duration,
                mediaMetadata.trackNumber,
                mediaMetadata.trackCount
            )
            this.mediaMetadata.postValue(nowPlayingMetadata)
        }

        checkSwitchState(mediaMetadata.trackNumber,  mediaMetadata.trackCount)

        // Update the media button resource ID
        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> R.drawable.ic_pause_black_24dp
                else -> R.mipmap.ic_media_play
            }
        )
    }
}

private const val POSITION_UPDATE_INTERVAL_MILLIS = 100L
