package xh.zero.tadpolestory.repo

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import xh.zero.core.vo.ApiResponse
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.repo.data.*

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
        @Query("category_id") categoryId: Int = Configs.CATEGORY_ID,
        @Query("tag_name") tagName: String? = null,
        // 返回结果排序维度：1-最火，2-最新，3-最多播放
        @Query("calc_dimension") calcDimension: Int = 1,
        @Query("page") page: Int,
        @Query("count") count: Int = Configs.PAGE_SIZE,
        // 是否输出付费内容（即返回值是否包含付费内容）：true-是； false-否；默认不填为 false
        @Query("contains_paid") isPaid: Boolean = false,
    ) : LiveData<ApiResponse<AlbumResponse>>

    @GET("$PREFIX/v2/search/albums")
    fun searchAlbums(
        @Query("industry_id") industry_id: Int? = null,
        @Query("id") id: Int? = null,
        @Query("title") title: String? = null,
        @Query("uid") uid: Int? = null,
        @Query("nickname") nickname: String? = null,
        @Query("tags") tags: String? = null,
        @Query("is_paid") is_paid: Int = 0,
        @Query("price_type") price_type: Int? = null,
        @Query("category_id") category_id: Long? = null,
        @Query("category_name") category_name: String? = null,
        @Query("sort_by") sort_by: String? = null,
        @Query("desc") desc: Boolean = true,
        @Query("page") page: Int,
        @Query("count") count: Int = Configs.PAGE_SIZE,
    ) : LiveData<ApiResponse<AlbumResponse>>

    @GET("$PREFIX/metadata/list")
    fun getMetadataList(
        @Query("category_id") category_id: Int = Configs.CATEGORY_ID
    ) : LiveData<ApiResponse<PlainData>>

    /**
     * 标签列表
     */
    @GET("$PREFIX/v2/tags/list")
    fun getTagList(
        @Query("category_id") categoryId: Int = Configs.CATEGORY_ID,
        @Query("type") type: Int = 0
    ) : LiveData<ApiResponse<List<AlbumTag>>>

    /**
     * 专辑浏览
     */
    @GET("$PREFIX/albums/browse")
    fun getVoiceListFormAlbum(
        @Query("album_id") album_id: String,
        @Query("sort") sort: String = "asc",
        @Query("page") page: Int,
        @Query("count") count: Int = Configs.PAGE_SIZE
    ) : Call<Album>

    /**
     * 每日推荐
     */
    @GET("$PREFIX/operation/recommend_albums")
    fun getDailyRecommendAlbums(
        @Query("access_token") access_token: String,
        @Query("page") page: Int,
        @Query("count") count: Int = 4
    ) : LiveData<ApiResponse<AlbumResponse>>

    /**
     * 猜你喜欢
     */
    @GET("$PREFIX/v2/albums/guess_like")
    fun getGuessLikeAlbums(
        // 返回几条结果数据，默认为 3，取值区间为[1,50]
        @Query("like_count") count: Int,
        @Query("device_type") deviceType: Int = 2
    ) : LiveData<ApiResponse<List<Album>>>

    /**
     * 获取临时token
     */
    @GET("$PREFIX/oauth2/secure_access_token")
    fun getTemporaryToken() : LiveData<ApiResponse<TemporaryToken>>

    @GET("$PREFIX/v2/tracks/relative_album")
    fun getRelativeAlbum(
        @Query("track_id") trackId: Int?
    ) : LiveData<ApiResponse<List<Album>>>

    /**
     * 多条件筛选
     */

}