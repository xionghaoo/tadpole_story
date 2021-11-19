package xh.zero.tadpolestory.repo

import androidx.lifecycle.LiveData
import retrofit2.http.GET
import retrofit2.http.Query
import xh.zero.core.vo.ApiResponse
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.repo.data.PlainData

/**
 * post 参数 application/x-www-form-urlencoded; charset=UTF-8
 */
interface ApiService {
    @GET("/ximalayaos-api/openapi-fmxos/get_login_url")
    fun getLoginUrl() : LiveData<ApiResponse<PlainData>>
}