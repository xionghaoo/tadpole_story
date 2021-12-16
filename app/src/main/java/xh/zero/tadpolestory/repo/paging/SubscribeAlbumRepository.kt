package xh.zero.tadpolestory.repo.paging

import retrofit2.Call
import retrofit2.Response
import xh.zero.core.paging.PagingDataFactory
import xh.zero.core.paging.PagingRepository
import xh.zero.core.utils.AppExecutors
import xh.zero.tadpolestory.repo.ApiService
import xh.zero.tadpolestory.repo.PreferenceStorage
import xh.zero.tadpolestory.repo.data.Album
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 推荐分页
 */
@Singleton
class SubscribeAlbumRepository @Inject constructor(
    appExecutors: AppExecutors,
    private val apiService: ApiService
) : PagingRepository<List<Album>, Album>(appExecutors) {
    override fun createSourceFactory(
        appExecutors: AppExecutors,
        params: List<String>
    ): PagingDataFactory<List<Album>, Album> = object : PagingDataFactory<List<Album>, Album>(appExecutors) {
        override fun convertToListData(r: List<Album>?): List<Album> {
            val albums = r
            return albums?.mapIndexed { index, item ->
                    if (index % 2 == 0) {
                        item.extraAlbum = if (index + 1 < albums.size) albums[index + 1] else null
                    }
                    item
                }?.filterIndexed { index, _ -> index % 2 == 0 } ?: emptyList()
        }

        override fun createAfterCall(pageNo: String): Call<List<Album>> {
            val page = pageNo.toInt()
            val start = (page - 1) * pageSize()
            val end = page * pageSize()
            // 1, 0 - 20
            // 2, 20 - 40
            // 3, 40 - 60
            // 4, 60 - 80
            // 5, 80 - 100
            val idsStr = StringBuilder()
            val ids = params[0].split(",")
                .filterIndexed { index, s -> index in start.until(end) }
            ids.forEach { id ->
                idsStr.append(id).append(",")
            }
            var param = idsStr.toString()
            // id为空时喜马拉雅接口会报错，这里补充一个不存在的id，用来做最后一页的查询
            if (ids.isEmpty()) {
               param = "1"
            }
            return apiService.getPagingAlbumsForIds(param)
        }

        override fun createInitialCall(): Call<List<Album>> {
            val idsStr = StringBuilder()
            val ids = params[0].split(",")
                .filterIndexed { index, s -> index in 0.until(pageSize()) }
            ids.forEach { id ->
                idsStr.append(id).append(",")
            }
            var param = idsStr.toString()
            if (ids.isEmpty()) {
                param = "1"
            }
            return apiService.getPagingAlbumsForIds(param)
        }

        override fun onResponse(response: Response<List<Album>>) {

        }
    }

    override fun pageSize(): Int = 20
}