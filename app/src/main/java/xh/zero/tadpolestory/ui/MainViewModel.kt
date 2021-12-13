package xh.zero.tadpolestory.ui

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
import com.example.android.uamp.media.extensions.id
import com.example.android.uamp.media.extensions.isPlayEnabled
import com.example.android.uamp.media.extensions.isPlaying
import com.example.android.uamp.media.extensions.isPrepared
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.Repository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val repo: Repository
) : ViewModel() {
    fun getLoginUrl() = repo.getLoginUrl()

    fun getTagList(id: Int) = repo.getMetadataList(id)
    fun getTemporaryToken() = repo.getTemporaryToken()
    fun getDailyRecommendAlbums(token: String, page: Int) = repo.getDailyRecommendAlbums(token, page)
    fun getGuessLikeAlbums() = repo.getGuessLikeAlbums()

//    fun getMetadataList() = repo.getMetadataList()

}