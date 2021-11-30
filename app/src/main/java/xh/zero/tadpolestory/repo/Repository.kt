package xh.zero.tadpolestory.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import xh.zero.tadpolestory.remoteRequestStrategy
import xh.zero.tadpolestory.repo.data.Album
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    @ApplicationContext val context: Context,
    private val apiService: ApiService,
    val prefs: SharedPreferenceStorage,
) {
    fun getLoginUrl() = remoteRequestStrategy {
        apiService.getLoginUrl()
    }

    fun getAccessToken() = remoteRequestStrategy {
        apiService.getAccessToken("123", "")
    }

    fun refreshToken() = remoteRequestStrategy {
        apiService.refreshToken(prefs.refreshToken)
    }

    fun getCategoriesList() = remoteRequestStrategy {
        apiService.getCategoriesList()
    }

    fun getAlbumsList() = remoteRequestStrategy {
        apiService.getAlbumsList(page = 1)
    }

    fun getTagList() = remoteRequestStrategy {
        apiService.getTagList()
    }

//    fun getVoiceListFormAlbum(albumId: Int, page: Int) = remoteRequestStrategy {
//        apiService.getVoiceListFormAlbum(album_id = albumId, page = page)
//    }

    fun getDailyRecommendAlbums(token: String, page: Int) = remoteRequestStrategy {
        apiService.getDailyRecommendAlbums(access_token = token, page = page)
    }


    fun getGuessLikeAlbums(count: Int) = remoteRequestStrategy {
        apiService.getGuessLikeAlbums(count)
    }

    fun getTemporaryToken() = remoteRequestStrategy {
        apiService.getTemporaryToken()
    }

    fun getRelativeAlbum(trackId: Int?) = remoteRequestStrategy {
        apiService.getRelativeAlbum(trackId)
    }

    fun getVoiceListFormAlbum(albumId: String, page: Int) = apiService.getVoiceListFormAlbum(album_id = albumId, page = page)
}