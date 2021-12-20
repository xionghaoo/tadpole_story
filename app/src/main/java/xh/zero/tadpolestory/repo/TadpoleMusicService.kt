package xh.zero.tadpolestory.repo

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import com.example.android.uamp.media.MusicService
import com.example.android.uamp.media.library.MusicSource
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.ui.MainActivity
import javax.inject.Inject

const val SEEK_TO_POSITION = "${Configs.PACKAGE_NAME}.COMMAND.SEEK_TO_POSITION"
const val PLAY_NEXT = "${Configs.PACKAGE_NAME}.COMMAND.PLAY_NEXT"
const val HAS_NEXT = "${Configs.PACKAGE_NAME}.COMMAND.HAS_NEXT"
const val PLAY_PREV = "${Configs.PACKAGE_NAME}.COMMAND.PLAY_PREV"
const val HAS_PREV = "${Configs.PACKAGE_NAME}.COMMAND.HAS_PREV"
const val EXTRA_MEDIA_POSITION = "${Configs.PACKAGE_NAME}.COMMAND.EXTRA_MEDIA_POSITION"
const val SEEK_TO_TRACK_INDEX = "${Configs.PACKAGE_NAME}.COMMAND.SEEK_TO_TRACK_INDEX"

const val LOAD_SONG_FOR_PAGE = "${Configs.PACKAGE_NAME}.COMMAND.LOAD_SONG_FOR_PAGE"
const val USE_CURRENT_LIST = "${Configs.PACKAGE_NAME}.COMMAND.USE_CURRENT_LIST"
const val SET_PLAY_SPEED = "${Configs.PACKAGE_NAME}.COMMAND.SET_PLAY_SPEED"
const val PLAY_SEEK = "${Configs.PACKAGE_NAME}.COMMAND.PLAY_SEEK"
const val AUTO_STOP = "${Configs.PACKAGE_NAME}.COMMAND.AUTO_STOP"
const val STOP_AFTER_TIME = "${Configs.PACKAGE_NAME}.COMMAND.STOP_AFTER_TIME"
const val GET_TIMING_START_TIME = "${Configs.PACKAGE_NAME}.COMMAND.GET_TIMING_START_TIME"
const val RESET_TIMING_CONFIG = "${Configs.PACKAGE_NAME}.COMMAND.RESET_TIMING_CONFIG"

const val ACTION_MEDIA_TIMING_STOP = "${Configs.PACKAGE_NAME}.ACTION.ACTION_MEDIA_TIMING_STOP"

typealias CommandHandler = (parameters: Bundle, callback: ResultReceiver?) -> Boolean

@AndroidEntryPoint
class TadpoleMusicService : MusicService() {

    @Inject
    lateinit var repo: Repository
    private val handler = Handler(Looper.myLooper()!!)

    private var autoStopCountIndex = 0
    private var autoStopCount = 0
    private var stopAfterTimeJob: Job? = null
    private var isTiming = false
    private var timingStartTime = 0L

    override fun onCreate() {
        super.onCreate()
        mediaSessionConnector.registerCustomCommandReceiver(TadpoleCommandReceiver())

        // TODO 上传播放记录
    }

    override fun onDestroy() {
        repo.prefs.nowPlayingAlbumId = null
        repo.prefs.nowPlayingAlbumTitle = null
        super.onDestroy()
    }

    override fun onPlayStateChange(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
            // 曲目自动切换监听
            if (autoStopCountIndex == 2) autoStopCount = 2
        }
    }

    private fun checkPlayPosition() {
        handler.postDelayed({
            if (isPlayEnd() && autoStopCount == autoStopCountIndex) {
                pause()
                Timber.d("停止播放")
                resetTimingConfig()

                repo.prefs.selectedTimingIndex = 0
            }
            if (isTiming) checkPlayPosition()
        }, 200)
    }

    /**
     * 播放完当前曲目自动停止
     */
    private fun stopOnThisEnd() {
        sendBroadcast(Intent(ACTION_MEDIA_TIMING_STOP))

        resetTimingConfig()
        isTiming = true
        autoStopCount = 1
        autoStopCountIndex = 1

        checkPlayPosition()
    }

    /**
     * 播放完下一曲目自动停止
     */
    private fun stopOnNextEnd() {
        sendBroadcast(Intent(ACTION_MEDIA_TIMING_STOP))

        resetTimingConfig()
        isTiming = true
        autoStopCount = 1
        autoStopCountIndex = 2

        checkPlayPosition()
    }

    private fun resetTimingConfig() {
        isTiming = false
        autoStopCount = 0
        autoStopCountIndex = 0
        stopAfterTimeJob?.cancel()
        stopAfterTimeJob = null
    }

    /**
     * 定时停止播放
     */
    private fun stopAfterTime(minute: Int) {
        resetTimingConfig()
        if (stopAfterTimeJob == null) {
            timingStartTime = System.currentTimeMillis()
            stopAfterTimeJob = CoroutineScope(Dispatchers.Default).launch {
                delay(minute * 60 * 1000L)
                Timber.d("停止曲目")
                withContext(Dispatchers.Main) {
                    pause()
                    stopAfterTimeJob?.cancel()
                    stopAfterTimeJob = null
                    resetTimingConfig()
                    repo.prefs.selectedTimingIndex = 0

                    sendBroadcast(Intent(ACTION_MEDIA_TIMING_STOP))
                }
            }
        }
    }

    override fun createMusicSource(): MusicSource {
        return TadpoleMusicSource(repo)
    }

    override fun createPendingIntent(): PendingIntent? {
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                sessionIntent.action = MainActivity.ACTION_NOTIFICATION_PLAYER
                PendingIntent.getActivity(this, 2, sessionIntent, 0)
            }
        return sessionActivityPendingIntent
    }

    private inner class TadpoleCommandReceiver : MediaSessionConnector.CommandReceiver {
        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ): Boolean = when (command) {
            SEEK_TO_POSITION -> seekToPositionCommand(extras ?: Bundle.EMPTY, cb)
            SEEK_TO_TRACK_INDEX -> seekToTrackIndex(extras ?: Bundle.EMPTY, cb)
            PLAY_PREV -> playPrevCommand(extras ?: Bundle.EMPTY, cb)
            PLAY_NEXT -> playNextCommand(extras ?: Bundle.EMPTY, cb)
            LOAD_SONG_FOR_PAGE -> loadSongForPageCommand(extras ?: Bundle.EMPTY, cb)
            USE_CURRENT_LIST -> useCurrentListCommand(extras ?: Bundle.EMPTY, cb)
            SET_PLAY_SPEED -> setPlaySpeedCommand(extras ?: Bundle.EMPTY, cb)
            PLAY_SEEK -> playSeekCommand(extras ?: Bundle.EMPTY, cb)
            AUTO_STOP -> autoStopCommand(extras ?: Bundle.EMPTY, cb)
            STOP_AFTER_TIME -> stopAfterTimeCommand(extras ?: Bundle.EMPTY, cb)
            GET_TIMING_START_TIME -> getTimingStartTimeCommand(extras ?: Bundle.EMPTY, cb)
            RESET_TIMING_CONFIG -> resetTimingConfigCommand(extras ?: Bundle.EMPTY, cb)
            else -> false
        }
    }

    private val seekToPositionCommand: CommandHandler = { extras, callback ->
        seekToPosition(extras.getLong(EXTRA_MEDIA_POSITION))
        callback?.send(Activity.RESULT_OK, Bundle.EMPTY)
        true
    }

    private val seekToTrackIndex: CommandHandler = { extras, callback ->
        seekTo(extras.getInt("index"))
        true
    }

    private val playPrevCommand: CommandHandler = { extras, callback ->
        val hasPrev = toPrev()
        callback?.send(Activity.RESULT_OK, Bundle().apply {
            putBoolean(HAS_PREV, hasPrev)
        })
        callback?.send(Activity.RESULT_OK, Bundle.EMPTY)
        true
    }

    private val playNextCommand: CommandHandler = { extras, callback ->
        val hasNext = toNext()
        callback?.send(Activity.RESULT_OK, Bundle().apply {
            putBoolean(HAS_NEXT, hasNext)
        })
        // 曲目手动切换监听
        if (autoStopCountIndex == 2) autoStopCount = 2
        callback?.send(Activity.RESULT_OK, Bundle.EMPTY)
        true
    }

    private val loadSongForPageCommand: CommandHandler = { extras, callback ->
        val mediaId = extras.getString("mediaId")
        val page = extras.getInt("page", 1)
        val isRefresh = extras.getBoolean("isRefresh", false)
        val isPaging = extras.getBoolean("isPaging", true)
        if (mediaId != null) {
            loadMedia(mediaId, page, isRefresh, isPaging)
        }
        true
    }

    private val useCurrentListCommand: CommandHandler = { extras, callback ->
        val mediaId = extras.getString("mediaId")
        if (mediaId != null) {
            useCurrentList(mediaId)
        }
        true
    }

    private val setPlaySpeedCommand: CommandHandler = { extras, callback ->
        val speed = extras.getFloat("speed")
        setPlaybackSpeed(speed)
        true
    }

    private val playSeekCommand: CommandHandler = { extras, callback ->
        val direction = extras.getInt("direction")
        if (direction == 0) {
            playForward15s()
        } else {
            playBackward15s()
        }
        callback?.send(Activity.RESULT_OK, Bundle.EMPTY)
        true
    }

    private val autoStopCommand: CommandHandler = { extras, callback ->
        val stopCount = extras.getInt("stop_count")
        if (stopCount == 1) {
            stopOnThisEnd()
        } else {
            stopOnNextEnd()
        }
        callback?.send(Activity.RESULT_OK, Bundle.EMPTY)
        true
    }

    private val stopAfterTimeCommand: CommandHandler = { extras, callback ->
        val minute = extras.getInt("minute")
        stopAfterTime(minute)
        callback?.send(Activity.RESULT_OK, Bundle().apply {
            putLong("startTime", timingStartTime)
        })
        true
    }

    private val getTimingStartTimeCommand: CommandHandler = { extras, callback ->
        callback?.send(Activity.RESULT_OK, Bundle().apply {
            putLong("startTime", timingStartTime)
        })
        true
    }

    private val resetTimingConfigCommand: CommandHandler = { extras, callback ->
        resetTimingConfig()
        callback?.send(Activity.RESULT_OK, Bundle.EMPTY)
        true
    }
}