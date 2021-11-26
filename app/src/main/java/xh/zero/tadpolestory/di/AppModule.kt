package xh.zero.tadpolestory.di

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.Build
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.media.MusicService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import xh.zero.core.network.LiveDataCallAdapterFactory
import xh.zero.core.utils.CryptoUtil
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.BuildConfig
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.repo.ApiService
import xh.zero.tadpolestory.repo.PreferenceStorage
import xh.zero.tadpolestory.repo.SharedPreferenceStorage
import xh.zero.tadpolestory.repo.TadpoleMusicService
import java.io.UnsupportedEncodingException
import java.lang.StringBuilder
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Singleton
import kotlin.Comparator
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        prefs: PreferenceStorage
    ): OkHttpClient {
        val logInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(logInterceptor)
//        if (BuildConfig.DEBUG) {
//            client.addInterceptor(mockInterceptor)
//        }
        client.addInterceptor { chain: Interceptor.Chain ->
            // 发起请求
            val request = chain.request()
            val newRequestBuilder = request.newBuilder()
                .addHeader("Content-Type", "application/json")
            // 登录接口占位符：invalid，其他接口会有具体的值
//                    ?.addHeader(
//                            preferenceStorage.tokenKey ?: "invalid",
//                            preferenceStorage.accessToken ?: "invalid"
//                    )
//                    ?.addHeader("Api-Key", "admin")
//                    ?.addHeader("Api-Secret", "SGeV1dFmCADUp8XWVpWObO62rIfbpf7Y")
            val newRequest = newRequestBuilder.url(createRequestUrl(request)).build()
            val response = chain.proceed(newRequest)

            // 接收响应
            val responseCode = response.code
            if (responseCode == 200) {
                val responseString = response.body?.string()
                var content: String? = responseString
                try {
//                    val gson = Gson()
//                    val res = gson.fromJson<PlainData>(content, PlainData::class.java)
//                    if (!ignoreRequestError && (res.code == 2042
//                            || res.code == 2044
//                            || res.code == 2031)) {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            prefs.clearCache()
//                            ToastUtil.showToast(context, res.msg ?: "")
//                            LoginActivity.startWithNewTask(context)
//                        }
//                    }
//                    ignoreRequestError = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val contentType = response.body?.contentType()
                val body = (responseString ?: "").toResponseBody(contentType)
                return@addInterceptor response.newBuilder().body(body).build()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    ToastUtil.show(context, "服务器请求错误")
                }
            }
            return@addInterceptor response
        }
        return client.build()
    }

    /**
     * 添加公共参数
     */
    private fun createRequestUrl(request: Request) : HttpUrl {
        val appKey = Configs.XIMALAYA_APP_KEY
        val timestamp = System.currentTimeMillis()
        val randStr = "${Random(timestamp).nextInt(10)}${
            Random(timestamp).nextLong(90000000) + 10000000
        }"
        val version = "1.0"
        val deviceId = Build.ID
        val deviceIdType = "Android_ID"
        val sn = Configs.XIMALAYA_SN
        // 对除sig外的所有参数进行签名
        val paramMap = TreeMap<String, Any?>(Comparator<String> { o1, o2 -> o1.compareTo(o2) })
        request.url.queryParameterNames.forEachIndexed { index, key ->
            val queryValue = request.url.queryParameterValue(index)
            paramMap[key] = queryValue
        }
        paramMap["app_key"] = appKey
        paramMap["client_os_type"] = 2
        paramMap["device_id"] = deviceId
        paramMap["device_id_type"] = deviceIdType
        paramMap["nonce"] = randStr
        paramMap["sn"] = sn
        paramMap["timestamp"] = timestamp
        paramMap["version"] = version
        val sigStrBuilder = StringBuilder()
        paramMap.forEach { (key, value) ->
            sigStrBuilder.append("&").append("$key=$value")
        }
        sigStrBuilder.replace(0, 1, "")
        // 公共参数
        return request.url.newBuilder()
            .addQueryParameter("app_key", appKey)
            .addQueryParameter("client_os_type", "2")
            .addQueryParameter("device_id", deviceId)
            .addQueryParameter("device_id_type", deviceIdType)
            // 随机字符串
            .addQueryParameter("nonce", randStr)
            .addQueryParameter("sn", sn)
            .addQueryParameter("timestamp", timestamp.toString())
            .addQueryParameter("version", version)
            .addQueryParameter("sig", generateSign(sigStrBuilder.toString()))
            .build()
    }

    /**
     * 生成参数校验签名
     */
    private fun generateSign(sigStr: String) : String {
        val bas64Str: String = Base64.getEncoder().encodeToString(sigStr.toByteArray())
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(Configs.XIMALAYA_APP_SECRET.toByteArray(), "HmacSHA1"))
        val sha1 = mac.doFinal(bas64Str.toByteArray())
        val md5 = MessageDigest.getInstance("MD5").digest(sha1)
        val hs = StringBuilder()
        for (b in md5) {
            val s = Integer.toHexString(b.toInt() and 0XFF)
            if (s.length == 1) {
                hs.append('0')
            }
            hs.append(s)
        }
        return hs.toString()
    }

    @Provides
    @Singleton
    fun provideApiService(client: OkHttpClient): ApiService =
        Retrofit.Builder()
            .baseUrl(Configs.HOST)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideSharedPreferences(application: Application): PreferenceStorage = SharedPreferenceStorage(application)

    @Provides
    @Singleton
    fun provideMusicServiceConnection(@ApplicationContext context: Context) = MusicServiceConnection.getInstance(
        context,
        ComponentName(context, TadpoleMusicService::class.java)
    )

}