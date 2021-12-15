package xh.zero.tadpolestory.repo.paging

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import retrofit2.Response
import xh.zero.core.paging.PagingDataFactory
import xh.zero.core.paging.PagingRepository
import xh.zero.core.utils.AppExecutors
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.repo.ApiService
import xh.zero.tadpolestory.repo.PreferenceStorage
import xh.zero.tadpolestory.repo.SharedPreferenceStorage
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.AlbumMetaData
import xh.zero.tadpolestory.repo.data.AlbumResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 推荐分页
 */
@Singleton
class RecommendAlbumRepository @Inject constructor(
    appExecutors: AppExecutors,
    private val apiService: ApiService,
    private val prefs: PreferenceStorage
) : PagingRepository<AlbumResponse, Album>(appExecutors) {
    override fun createSourceFactory(
        appExecutors: AppExecutors,
        params: List<String>
    ): PagingDataFactory<AlbumResponse, Album> = object : PagingDataFactory<AlbumResponse, Album>(appExecutors) {
        override fun convertToListData(r: AlbumResponse?): List<Album> {
            val category = params[0].toInt()
            val albums = r?.albums?.filter { album -> album.category_id == category }
            return albums?.mapIndexed { index, item ->
                    if (index % 2 == 0) {
                        item.extraAlbum = if (index + 1 < albums.size) albums[index + 1] else null
                    }
                    item
                }?.filterIndexed { index, _ -> index % 2 == 0 } ?: emptyList()
        }

        override fun createAfterCall(pageNo: String): Call<AlbumResponse> {
            return apiService.getPagingDailyRecommendAlbums(
                page = pageNo.toInt(),
                count = Configs.PAGE_SIZE,
                access_token = prefs.accessToken.orEmpty()
            )
        }

        override fun createInitialCall(): Call<AlbumResponse> {
            return apiService.getPagingDailyRecommendAlbums(
                page = 1,
                count = Configs.PAGE_SIZE,
                access_token = prefs.accessToken.orEmpty()
            )
        }

        override fun onResponse(response: Response<AlbumResponse>) {

        }
    }

    override fun pageSize(): Int = Configs.PAGE_SIZE
}