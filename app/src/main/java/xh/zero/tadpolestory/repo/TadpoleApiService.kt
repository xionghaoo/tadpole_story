package xh.zero.tadpolestory.repo

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import xh.zero.core.vo.ApiResponse
import xh.zero.tadpolestory.repo.data.IsSubscribe
import xh.zero.tadpolestory.repo.data.PlainData
import xh.zero.tadpolestory.repo.data.SubscribeIdsResult

interface TadpoleApiService {
    companion object {
        const val PREFIX = "/v1/smartlamp-api"
    }

    /**
     * 订阅专辑
     */
    @POST("$PREFIX/common/collect/1")
    fun subscribeAlbum(
        @Query("albumId") albumId: Int
    ) : LiveData<ApiResponse<PlainData>>

    /**
     * 上报听故事记录
     */
    @POST("$PREFIX/common/collect/2")
    fun uploadListenRecord(
        @Query("albumId") albumId: Int
    ) : Call<PlainData>

    @GET("$PREFIX/common/recent/list")
    fun getRecentAlbumsIds() : LiveData<ApiResponse<SubscribeIdsResult>>

    @GET("$PREFIX/common/collect/list")
    fun getSubscribeAlbumsIds() : LiveData<ApiResponse<SubscribeIdsResult>>

    /**
     * 取消订阅
     */
    @POST("$PREFIX/common/cancel/collect/{albumId}")
    fun unsubscribe(@Path("albumId") albumId: Int) : LiveData<ApiResponse<PlainData>>

    /**
     * 查询是否订阅
     */
    @GET("$PREFIX/common/query/collect/{albumId}")
    fun isSubscribe(@Path("albumId") albumId: Int) : LiveData<ApiResponse<IsSubscribe>>
}