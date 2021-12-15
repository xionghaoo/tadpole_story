package xh.zero.tadpolestory.repo

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import xh.zero.core.vo.ApiResponse
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.repo.data.PlainData

interface TadpoleApiService {
    companion object {
        const val PREFIX = "/v1/smartlamp-api/"
    }

    @POST("$PREFIX/common/collect/{albumId}")
    fun subscribeAlbum(@Path("albumId") albumId: Int) : LiveData<ApiResponse<PlainData>>

    @GET("$PREFIX/common/recent/page")
    fun getSubscribeAlbumsIds(
        @Query("limit") limit: Int,
        @Query("page") page: Int = Configs.PAGE_SIZE
    ) : LiveData<ApiResponse<PlainData>>
}