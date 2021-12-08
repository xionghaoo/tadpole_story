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
import xh.zero.tadpolestory.repo.SharedPreferenceStorage
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.AlbumMetaData
import xh.zero.tadpolestory.repo.data.AlbumResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 筛选分页
 */
@Singleton
class AlbumRepository @Inject constructor(
    appExecutors: AppExecutors,
    private val apiService: ApiService
) : PagingRepository<AlbumResponse, Album>(appExecutors) {
    override fun createSourceFactory(
        appExecutors: AppExecutors,
        params: List<String>
    ): PagingDataFactory<AlbumResponse, Album> = object : PagingDataFactory<AlbumResponse, Album>(appExecutors) {
        override fun convertToListData(r: AlbumResponse?): List<Album> {
//            var tmp: Album? = null
//            r?.albums?.forEachIndexed { index, album ->
//                if (index % 2 == 0) {
//                    // 偶数位，记录数据
//                    tmp = album
//                } else {
//                    // 奇数位，附加数据
//                    album.extraAlbum = tmp
//                }
//            }
//            return r?.albums?.filterIndexed { index, _ -> index % 2 != 0 } ?: emptyList()

            return r?.albums?.filterIndexed { index, _ -> index % 2 == 0 }
                ?.mapIndexed { index, item ->
                    item.extraAlbum = if (index + 1 < r.albums.size) r.albums[index + 1] else null
                    item
                } ?: emptyList()
        }

        override fun createAfterCall(pageNo: String): Call<AlbumResponse> {
            return apiService.getMetadataAlbums(
                page = pageNo.toInt(),
                count = Configs.PAGE_SIZE,
                metadata_attributes = params[0],
                calc_dimension = params[1].toInt(),
            )
        }

        override fun createInitialCall(): Call<AlbumResponse> {
            return apiService.getMetadataAlbums(
                page = 1,
                count = Configs.PAGE_SIZE,
                metadata_attributes = params[0],
                calc_dimension = params[1].toInt(),
            )
        }

        override fun onResponse(response: Response<AlbumResponse>) {

        }
    }

    override fun pageSize(): Int = Configs.PAGE_SIZE
}