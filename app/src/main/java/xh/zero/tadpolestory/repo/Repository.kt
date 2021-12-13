package xh.zero.tadpolestory.repo

import android.content.Context
import androidx.lifecycle.LiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xh.zero.core.utils.SystemUtil
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.remoteRequestStrategy
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.tables.SearchHistory
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    @ApplicationContext val context: Context,
    private val apiService: ApiService,
    val prefs: SharedPreferenceStorage,
    private val db: CacheDatabase
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

    fun getAlbumsList(page: Int = 1, calcDimension: Int, categoryId: Int, pageSize: Int = Configs.PAGE_SIZE) = remoteRequestStrategy {
        apiService.getAlbumsList(page = page, calcDimension = calcDimension, count = pageSize, categoryId = categoryId)
    }

    fun searchAlbums(page: Int, tags: String) = remoteRequestStrategy {
        apiService.searchAlbums(page = page, tags = tags)
    }

    fun getAlbumsForIds(ids: String) = remoteRequestStrategy {
        apiService.getAlbumsForIds(ids)
    }

    fun getHotKeyword(top: Int, categoryId: Int) = remoteRequestStrategy {
        apiService.getHotKeyword(top = top, category_id = categoryId)
    }

    fun getSearchWords(q: String) = remoteRequestStrategy {
        apiService.getSearchWords(q)
    }

//    fun getTagList() = remoteRequestStrategy {
//        apiService.getTagList()
//    }

    fun getMetadataList(categoryId: Int) = remoteRequestStrategy {
        apiService.getMetadataList(category_id = categoryId)
    }

    fun getDailyRecommendAlbums(token: String, page: Int) = remoteRequestStrategy {
        apiService.getDailyRecommendAlbums(access_token = token, page = page)
    }


    fun getGuessLikeAlbums() = remoteRequestStrategy {
        apiService.getGuessLikeAlbums()
    }

    fun getTemporaryToken() = remoteRequestStrategy {
        apiService.getTemporaryToken()
    }

    fun getRelativeAlbum(trackId: Int?) = remoteRequestStrategy {
        apiService.getRelativeAlbum(trackId)
    }

    fun getVoiceListFormAlbum(albumId: String, page: Int, pageSize: Int? = null) =
        apiService.getVoiceListFormAlbum(album_id = albumId, page = page, count = pageSize)

    fun saveSearchHistory(txt: String) {
        CoroutineScope(Dispatchers.Default).launch {
            db.searchHistoryDao().insert(SearchHistory().apply {
                keyword = txt
                created = System.currentTimeMillis()
            })
        }
    }

    fun loadAllSearchHistory() : LiveData<List<SearchHistory>> = db.searchHistoryDao().findAll()

    fun clearSearchHistory() {
        CoroutineScope(Dispatchers.Default).launch {
            db.searchHistoryDao().clear()
        }
    }

    fun savePlayingAlbum(album: Album) {
        CoroutineScope(Dispatchers.Default).launch {
            album.createdTime = System.currentTimeMillis()
            db.albumDao().insert(album)
        }
    }

    fun loadAllAlbums() = db.albumDao().findAll()

    fun findCurrentAlbum(complete: (Album?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            val item = db.albumDao().find()
            withContext(Dispatchers.Main) {
                complete(item)
            }
        }
    }
}