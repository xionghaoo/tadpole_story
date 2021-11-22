package xh.zero.tadpolestory.repo

import androidx.lifecycle.LiveData
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import xh.zero.core.vo.ApiResponse
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.repo.data.AlbumResponse
import xh.zero.tadpolestory.repo.data.Category
import xh.zero.tadpolestory.repo.data.GetLoginUrlResult
import xh.zero.tadpolestory.repo.data.PlainData

/**
 * post 参数 application/x-www-form-urlencoded; charset=UTF-8
 */
interface ApiService {

    companion object {
        const val PREFIX = "/ximalayaos-api/openapi-fmxos"
    }

    @GET("$PREFIX/get_login_url")
    fun getLoginUrl() : LiveData<ApiResponse<GetLoginUrlResult>>

    @FormUrlEncoded
    @POST("$PREFIX/oauth2/v2/authorize")
    fun getAccessToken(
        @Query("uid") uid: String,
        @Query("token") token: String,
//        @Query("domain") domain: String
    ) : LiveData<ApiResponse<PlainData>>

    @FormUrlEncoded
    @POST("$PREFIX/oauth2/refresh_token")
    fun refreshToken(
        @Query("refresh_token") refreshToken: String?
    ) : LiveData<ApiResponse<PlainData>>

    @GET("$PREFIX/categories/list")
    fun getCategoriesList() : LiveData<ApiResponse<List<Category>>>

    /**
     * 专辑列表
     */
    @GET("$PREFIX/v2/albums/list")
    fun getAlbumsList(
        @Query("category_id") categoryId: Int = 92,
        @Query("tag_name") tagName: String? = null,
        // 返回结果排序维度：1-最火，2-最新，3-最多播放
        @Query("calc_dimension") calcDimension: Int = 1,
        @Query("page") page: Int,
        @Query("count") count: Int = 20,
        // 是否输出付费内容（即返回值是否包含付费内容）：true-是； false-否；默认不填为 false
        @Query("contains_paid") isPaid: Boolean = false,
    ) : LiveData<ApiResponse<AlbumResponse>>

    @GET("$PREFIX/v2/tags/list")
    fun getTagList(
        @Query("category_id") categoryId: Int = 92,
        @Query("type") type: Int = 0
    ) : LiveData<ApiResponse<PlainData>>
}