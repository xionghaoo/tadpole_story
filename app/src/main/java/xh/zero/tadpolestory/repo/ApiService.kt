package xh.zero.tadpolestory.repo

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.http.*
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
        @Query("category_id") categoryId: Int,
        @Query("tag_name") tagName: String? = null,
        // 返回结果排序维度：1-最火，2-最新，3-最多播放
        @Query("calc_dimension") calcDimension: Int = 1,
        @Query("page") page: Int,
        @Query("count") count: Int,
        // 是否输出付费内容（即返回值是否包含付费内容）：true-是； false-否；默认不填为 false
        @Query("contains_paid") isPaid: Boolean = false,
    ) : LiveData<ApiResponse<AlbumResponse>>

    /**
     * 专辑列表
     */
    @GET("$PREFIX/v2/albums/list")
    fun getAlbumPagingList(
        @Query("category_id") categoryId: Int,
        @Query("tag_name") tagName: String? = null,
        // 返回结果排序维度：1-最火，2-最新，3-最多播放
        @Query("calc_dimension") calcDimension: Int = 1,
        @Query("page") page: Int,
        @Query("count") count: Int,
        // 是否输出付费内容（即返回值是否包含付费内容）：true-是； false-否；默认不填为 false
        @Query("contains_paid") isPaid: Boolean = false,
    ) : Call<AlbumResponse>

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

    @GET("$PREFIX/albums/get_batch")
    fun getAlbumsForIds(
        @Query("ids") ids: String
    ) : LiveData<ApiResponse<List<Album>>>

    @GET("$PREFIX/albums/get_batch")
    fun getPagingAlbumsForIds(
        @Query("ids") ids: String
    ) : Call<List<Album>>

    /**
     * 关键词搜索专辑
     */
    @GET("$PREFIX/search/albums")
    fun searchAlbums(
        @Query("q") q: String,
        @Query("industry_id") industry_id: Int? = null,
        @Query("category_id") category_id: Int? = null,
        @Query("calc_dimension") calc_dimension: Int? = null,
        @Query("page") page: Int,
        @Query("count") count: Int = Configs.PAGE_SIZE,
    ) : Call<AlbumResponse>

    /**
     * 热搜词
     */
    @GET("$PREFIX/search/hot_words")
    fun getHotKeyword(
        @Query("top") top: Int,
        @Query("category_id") category_id: Int
    ) : LiveData<ApiResponse<List<HotKeyword>>>

    @GET("$PREFIX/search/suggest_words")
    fun getSearchWords(
        @Query("q") q: String
    ) : LiveData<ApiResponse<SearchWordResult>>

    /**
     * 获取专辑元数据
     */
    @GET("$PREFIX/metadata/list")
    fun getMetadataList(
        @Query("category_id") category_id: Int
    ) : LiveData<ApiResponse<List<AlbumMetaData>>>

    /**
     * 根据专辑元数据获取专辑
     */
    @GET("$PREFIX/metadata/albums")
    fun getMetadataAlbums(
        // 分类 ID。分类数据可以通过 /categories/list 获取
        @Query("category_id") categoryId: Int,
        // 元数据属性列表:在/metadata/list 接口得到的结果中，取不同元 数据属性的 attrkey 和 atrrvalue 组成任意个数的 key-value 键值， 格式
        // 如: attr_key1:attr_value1;attr_key2:attr_value2;attr_key3 :attr_value3。注意: 此字段可为空，为空表示获取此分类下全部
        @Query("metadata_attributes") metadata_attributes: String? = null,
        // 返回结果排序维度:1-最火，2-最新，3-最多播放
        @Query("calc_dimension") calc_dimension: Int,
        // 是否包含付费内容，true 或者 false，默认:false
        @Query("contains_paid") contains_paid: Boolean = false,
        // 是否只输出付费内容，true 或者 false，默认:false
        @Query("only_paid") only_paid: Boolean = false,
        @Query("page") page: Int,
        @Query("count") count: Int = Configs.PAGE_SIZE,
    ) : Call<AlbumResponse>

    /**
     * 标签列表
     */
    @GET("$PREFIX/v2/tags/list")
    fun getTagList(
        @Query("category_id") categoryId: Int,
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
        @Query("count") count: Int? = Configs.PAGE_SIZE
    ) : Call<Album>

    /**
     * 每日推荐
     */
    @GET("$PREFIX/operation/recommend_albums")
    fun getDailyRecommendAlbums(
        @Query("access_token") access_token: String,
        @Query("page") page: Int,
        // 最大是200
        @Query("count") count: Int = 200
    ) : LiveData<ApiResponse<AlbumResponse>>

    /**
     * 每日推荐
     */
    @GET("$PREFIX/operation/recommend_albums")
    fun getPagingDailyRecommendAlbums(
        @Query("access_token") access_token: String,
        @Query("page") page: Int,
        @Query("count") count: Int = Configs.PAGE_SIZE
    ) : Call<AlbumResponse>

    /**
     * 播放记录上报
     */
    @FormUrlEncoded
    @POST("$PREFIX/openapi-collector-app/track_batch_records")
    fun uploadPlayRecords(
        @Field("track_records") track_records: String,
        @Field("access_token") access_token: String?
    ) : Call<PlainData>

    /**
     * 猜你喜欢
     */
    @GET("$PREFIX/v2/albums/guess_like")
    fun getGuessLikeAlbums(
        // 返回几条结果数据，默认为 3，取值区间为[1,50]
        @Query("like_count") count: Int = 50,
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