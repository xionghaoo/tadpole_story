package xh.zero.tadpolestory.ui.album

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
import com.google.android.exoplayer2.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.*
import xh.zero.tadpolestory.repo.data.PlainData
import xh.zero.tadpolestory.repo.data.TrackPlayRecord
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    val repo: Repository,
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
    private var updateBufferPosition = true
    private val handler = Handler(Looper.getMainLooper())

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE
    val mediaMetadata = MutableLiveData<NowPlayingMetadata>()
    val mediaPosition = MutableLiveData<Long>().apply {
        postValue(0L)
    }
    val mediaProgress = MutableLiveData<Int>().apply {
        postValue(0)
    }
    var bufferProgress = 0

    val mediaButtonRes = MutableLiveData<Int>().apply {
        postValue(R.mipmap.ic_media_play)
    }
    val switchState = MutableLiveData<Pair<Boolean, Boolean>>().apply {
        postValue(Pair(first = false, second = true))
    }
    private var stopAfterTimeJob: Job? = null

    var isPlaying: Boolean = false

    private var autoStopCountIndex = 0
    private var autoStopCount = 0

    fun getRelativeAlbum(trackId: Int) = repo.getRelativeAlbum(trackId)

    /**
     * When the session's [PlaybackStateCompat] changes, the [mediaItems] need to be updated
     * so the correct [MediaItemData.playbackRes] is displayed on the active item.
     * (i.e.: play/pause button or blank)
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        Timber.d("playbackStateObserver: ${it.playbackState}")
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

    private val trackSwitchStateObserver = Observer<Int?> { state ->
        if (playbackState.isPlaying && state == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
            // 曲目自动切换监听
            if (autoStopCountIndex == 2) autoStopCount = 2
        }
    }

    private val musicServiceConnection = musicServiceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
        it.trackSwitchState.observeForever(trackSwitchStateObserver)
        checkPlaybackPosition()
    }

    /**
     * Internal function that recursively calls itself every [POSITION_UPDATE_INTERVAL_MILLIS] ms
     * to check the current playback position and updates the corresponding LiveData object when it
     * has changed.
     */
    private fun checkPlaybackPosition(): Boolean = handler.postDelayed({
        val currPosition = playbackState.currentPlayBackPosition
        val totalDuration = mediaMetadata.value?.duration ?: 0L

        // 播放到结尾，自动停止
        if (totalDuration in 1..currPosition && playbackState.isPlaying && autoStopCountIndex > 0) {
            if (autoStopCount == autoStopCountIndex) {
                musicServiceConnection.transportControls.pause()
            }
        }

        if (mediaPosition.value != currPosition) {
            mediaPosition.postValue(currPosition)
            mediaProgress.postValue(((currPosition.toFloat() / totalDuration) * NowPlayingFragment.MAX_PROGRESS).roundToInt())

            if (totalDuration > 0) {
                val bufferPosition = playbackState.bufferedPosition
                bufferProgress = ((bufferPosition.toFloat() / totalDuration) * NowPlayingFragment.MAX_PROGRESS).roundToInt()
            }
        }

        if (updatePosition) checkPlaybackPosition()
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

    private fun rePlay() {
        musicServiceConnection.playbackState.value?.let { playbackState ->
            if (!playbackState.isPlaying) {
                mediaMetadata.value?.let {
                    playMediaId(it.id)
                }
            }
        }
    }

    fun seekToPosition(posMs: Long, complete: () -> Unit) {
        musicServiceConnection.sendCommand(SEEK_TO_POSITION, Bundle().apply {
            putLong(EXTRA_MEDIA_POSITION, posMs)
        }) { _, _ ->
            rePlay()
            complete()
        }
    }

    fun next() {
        musicServiceConnection.sendCommand(PLAY_NEXT, Bundle.EMPTY) { code, bundle ->
            rePlay()
            // 曲目手动切换监听
            if (autoStopCountIndex == 2) autoStopCount = 2
        }
    }

    fun prev() {
        musicServiceConnection.sendCommand(PLAY_PREV, Bundle.EMPTY) { code, bundle ->
            rePlay()
        }
    }

    fun setPlaySpeed(speed: Float) {
        musicServiceConnection.sendCommand(SET_PLAY_SPEED, Bundle().apply {
            putFloat("speed", speed)
        }) { _, _ ->

        }
    }

    fun playForward15s() {
        musicServiceConnection.sendCommand(PLAY_SEEK, Bundle().apply {
            putInt("direction", 0)
        }) { _, _ ->
            rePlay()
        }
    }

    fun playBackward15s() {
        musicServiceConnection.sendCommand(PLAY_SEEK, Bundle().apply {
            putInt("direction", 1)
        }) { _, _ ->
            rePlay()
        }
    }

    /**
     * 播放完当前曲目自动停止
     */
    fun stopOnThisEnd() {
        resetTimingConfig()
        autoStopCount = 1
        autoStopCountIndex = 1
    }

    /**
     * 播放完下一曲目自动停止
     */
    fun stopOnNextEnd() {
        resetTimingConfig()
        autoStopCount = 1
        autoStopCountIndex = 2
    }

    /**
     * 定时停止播放
     */
    fun stopAfterTime(minute: Int) {
        resetTimingConfig()
        if (stopAfterTimeJob == null) {
            stopAfterTimeJob = CoroutineScope(Dispatchers.Default).launch {
                delay(minute * 60 * 1000L)
                Timber.d("停止曲目")
                musicServiceConnection.transportControls.pause()
                stopAfterTimeJob?.cancel()
                stopAfterTimeJob = null
            }
        }
    }

    fun resetTimingConfig() {
        autoStopCount = 0
        autoStopCountIndex = 0
        stopAfterTimeJob?.cancel()
        stopAfterTimeJob = null
    }

    private fun checkSwitchState(orderNum: Long, totalNum: Long) {
        if (totalNum <= 1L) {
            switchState.postValue(Pair(first = false, second = false))
            return
        }
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
        musicServiceConnection.trackSwitchState.removeObserver(trackSwitchStateObserver)

        // Stop updating the position
        updatePosition = false
        updateBufferPosition = false
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
        isPlaying = playbackState.isPlaying
    }

    /**
     * 上传播放记录
     */
    fun uploadRecords(mediaMetadata: NowPlayingMetadata) {
        if (repo.prefs.nowPlayingAlbumId?.isNotEmpty() == true
            && mediaMetadata.id.isNotEmpty()
        ) {
            Timber.d("上传播放记录")
            val tracks = ArrayList<TrackPlayRecord>().apply {
                add(
                    TrackPlayRecord(
                        track_id = mediaMetadata.id.toInt(),
                        album_id = repo.prefs.nowPlayingAlbumId!!.toInt(),
                        duration = Math.floor(mediaMetadata.duration.toInt() / 1E3).toInt(),
                        started_at = (System.currentTimeMillis() / 1000 - (mediaPosition.value ?: 0) / 1000).toInt(),
                        played_secs = Math.floor((mediaPosition.value ?: 0) / 1E3).toInt()
                    )
                )
            }
            repo.uploadPlayRecords(tracks).enqueue(object : Callback<PlainData> {
                override fun onResponse(
                    call: Call<PlainData>,
                    response: Response<PlainData>
                ) {
                    if (response.isSuccessful) {
                        Timber.d("上传播放记录成功")
                    }
                }

                override fun onFailure(call: Call<PlainData>, t: Throwable) {
                }
            })
        }
    }

}

private const val POSITION_UPDATE_INTERVAL_MILLIS = 100L
