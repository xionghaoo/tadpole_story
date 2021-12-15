package xh.zero.tadpolestory.repo

import androidx.lifecycle.LiveData
import retrofit2.http.POST
import retrofit2.http.Path
import xh.zero.core.vo.ApiResponse
import xh.zero.tadpolestory.repo.data.PlainData

interface TadpoleApiService {
    companion object {
        const val PREFIX = "/v1/smartlamp-api/"
    }
    @POST("$PREFIX/common/collect/{albumId}")
    fun subscribeAlbum(@Path("albumId") albumId: Int) : LiveData<ApiResponse<PlainData>>
}