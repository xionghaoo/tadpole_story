package xh.zero.tadpolestory.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import xh.zero.tadpolestory.remoteRequestStrategy
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
}