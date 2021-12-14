package xh.zero.tadpolestory.ui

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.android.uamp.common.EMPTY_PLAYBACK_STATE
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.common.NOTHING_PLAYING
import com.example.android.uamp.media.extensions.*
import com.google.android.exoplayer2.Player
import dagger.assisted.Assisted
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
import xh.zero.tadpolestory.ui.album.NowPlayingFragment
import xh.zero.tadpolestory.ui.album.NowPlayingViewModel
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MainViewModel @Inject constructor(
    val repo: Repository
) : ViewModel() {
    fun getTagList(id: Int) = repo.getMetadataList(id)
    fun getTemporaryToken() = repo.getTemporaryToken()
    fun getDailyRecommendAlbums(token: String, page: Int) = repo.getDailyRecommendAlbums(token, page)
    fun getGuessLikeAlbums() = repo.getGuessLikeAlbums()
}

private const val POSITION_UPDATE_INTERVAL_MILLIS = 100L
