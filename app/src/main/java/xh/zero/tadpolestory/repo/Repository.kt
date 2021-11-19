package xh.zero.tadpolestory.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import xh.zero.tadpolestory.remoteRequestStrategy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    @ApplicationContext val context: Context,
    private val apiService: ApiService,
    val prefs: SharedPreferenceStorage,
) {
    fun getLoginUrl() = remoteRequestStrategy {
        apiService.getLoginUrl()
    }
}