package xh.zero.tadpolestory.repo

import android.app.Activity
import android.app.PendingIntent
import android.os.Bundle
import android.os.ResultReceiver
import com.example.android.uamp.media.MusicService
import com.example.android.uamp.media.library.MusicSource
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.ui.MainActivity
import javax.inject.Inject

const val SEEK_TO_POSITION = "${Configs.PACKAGE_NAME}.COMMAND.SEEK_TO_POSITION"
const val PLAY_NEXT = "${Configs.PACKAGE_NAME}.COMMAND.PLAY_NEXT"
const val HAS_NEXT = "${Configs.PACKAGE_NAME}.COMMAND.HAS_NEXT"
const val PLAY_PREV = "${Configs.PACKAGE_NAME}.COMMAND.PLAY_PREV"
const val HAS_PREV = "${Configs.PACKAGE_NAME}.COMMAND.HAS_PREV"
const val EXTRA_MEDIA_POSITION = "${Configs.PACKAGE_NAME}.COMMAND.EXTRA_MEDIA_POSITION"

const val LOAD_SONG_FOR_PAGE = "${Configs.PACKAGE_NAME}.COMMAND.LOAD_SONG_FOR_PAGE"
const val SET_PLAY_SPEED = "${Configs.PACKAGE_NAME}.COMMAND.SET_PLAY_SPEED"
const val PLAY_SEEK = "${Configs.PACKAGE_NAME}.COMMAND.PLAY_SEEK"

typealias CommandHandler = (parameters: Bundle, callback: ResultReceiver?) -> Boolean

@AndroidEntryPoint
class TadpoleMusicService : MusicService() {

    @Inject
    lateinit var repo: Repository

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
            PLAY_PREV -> playPrevCommand(extras ?: Bundle.EMPTY, cb)
            PLAY_NEXT -> playNextCommand(extras ?: Bundle.EMPTY, cb)
            LOAD_SONG_FOR_PAGE -> loadSongForPageCommand(extras ?: Bundle.EMPTY, cb)
            SET_PLAY_SPEED -> setPlaySpeedCommand(extras ?: Bundle.EMPTY, cb)
            PLAY_SEEK -> playSeekCommand(extras ?: Bundle.EMPTY, cb)
            else -> false
        }
    }

    private val seekToPositionCommand: CommandHandler = { extras, callback ->
        seekToPosition(extras.getLong(EXTRA_MEDIA_POSITION))
        callback?.send(Activity.RESULT_OK, Bundle.EMPTY)
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
}