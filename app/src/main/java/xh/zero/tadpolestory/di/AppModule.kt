package xh.zero.tadpolestory.di

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import xh.zero.core.network.LiveDataCallAdapterFactory
import xh.zero.core.utils.CryptoUtil
import xh.zero.tadpolestory.BuildConfig
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.repo.ApiService
import xh.zero.tadpolestory.repo.PreferenceStorage
import xh.zero.tadpolestory.repo.SharedPreferenceStorage
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Singleton
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
            val randStr = "${Random(System.currentTimeMillis()).nextInt(10)}${
                Random(System.currentTimeMillis()).nextLong(900000000) + 100000000
            }"
            val timestamp = System.currentTimeMillis().toString()
            val sigStr = "app_key=${Configs.XIMALAYA_APP_KEY}&sn=${Configs.XIMALAYA_SN}&" +
                "client_os_type=2&device_id_type=Android_ID&device_id=${Build.ID}&" +
                "nonce=${randStr}&timestamp=${timestamp}&version=${BuildConfig.VERSION_NAME}"
            Timber.d("sigStr: $sigStr")
            val bas64Str: String = String(Base64.encodeBase64(sigStr.toByteArray()))
            Timber.d("base64Str: $bas64Str")
//            val signKey = SecretKeySpec(Configs.XIMALAYA_APP_KEY.toByteArray(), "HmacSHA1")
//            val mac = Mac.getInstance("HmacSHA1")
//            mac.init(signKey)
//            val sig = bytesToHex(mac.doFinal(bas64Str.toByteArray()))
//            val sig = HmacUtils(HmacAlgorithms.HMAC_SHA_1, Configs.XIMALAYA_APP_KEY.toByteArray()).hmacHex(bas64Str)
            val sigSha1 = hmacSha1(bas64Str, Configs.XIMALAYA_APP_KEY)
            Timber.d("sigSha1: $sigSha1")
            val sigMd5 = CryptoUtil.encryptToMD5(sigSha1)
            Timber.d("sigMd5: $sigMd5")
            val httpUrl = request.url.newBuilder()
                .addQueryParameter("app_key", Configs.XIMALAYA_APP_KEY)
                .addQueryParameter("sn", Configs.XIMALAYA_SN)
                .addQueryParameter("client_os_type", "2")
                .addQueryParameter("device_id_type", "Android_ID")
                .addQueryParameter("device_id", Build.ID)
                // 随机字符串
                .addQueryParameter("nonce", randStr)
                .addQueryParameter("timestamp", timestamp)
                .addQueryParameter("version", BuildConfig.VERSION_NAME)
                .addQueryParameter("sig", sigMd5)
                .build()
            val newRequestBuilder = request.newBuilder()
                .addHeader("Content-Type", "application/json")
//                .addHeader("client_os_type", "")
//                .addHeader("device_id_type", "")
//                .addHeader("device_id", "")

//                .addHeader("X-UBT-AppId", Configs.ubtAppId)
//                .addHeader("product", Configs.ubtProduct)
            // 登录接口占位符：invalid，其他接口会有具体的值
//                    ?.addHeader(
//                            preferenceStorage.tokenKey ?: "invalid",
//                            preferenceStorage.accessToken ?: "invalid"
//                    )
//                    ?.addHeader("Api-Key", "admin")
//                    ?.addHeader("Api-Secret", "SGeV1dFmCADUp8XWVpWObO62rIfbpf7Y")
            // 如果device id为空，那么随机生成一个8位的id
//            val deviceId = prefs.serialNumber ?: (Random(1).nextInt(90000000) + 10000000).toString()
//            val androidID: String = Settings.Secure.getString(
//                context.contentResolver,
//                Settings.Secure.ANDROID_ID
//            )
//            if (androidID == null) {
//
//            }
//            prefs.serialNumber = deviceId
//            refreshSign(newRequestBuilder, prefs.serialNumber)
            val newRequest = newRequestBuilder.url(httpUrl).build()
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
            }

//            if (responseCode == 403) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    prefs.clearCache()
//                    ToastUtil.showToast(context, "无法确认设备ID")
//                    LoginActivity.startWithNewTask(context)
//                }
//            }

            if (responseCode >= 500) {
                CoroutineScope(Dispatchers.Main).launch {
//                    ToastUtil.showToast(context, "服务器请求错误")
                }
            }

            if (responseCode >= 300) {
                val failureUrl = newRequest.url.toString()
                if (failureUrl.isNotEmpty()) {
                    Timber.d("request failure: " + responseCode.toString() + ", " + failureUrl)
                }
            }
            return@addInterceptor response
        }
        return client.build()
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

    fun bytesToHex(bytes: ByteArray): String? {
        val result = CharArray(bytes.size * 2)
        for (index in bytes.indices) {
            val v = bytes[index].toInt()
            val upper = v ushr 4 and 0xF
            result[index * 2] = (upper + if (upper < 10) 48 else 65 - 10).toChar()
            val lower = v and 0xF
            result[index * 2 + 1] = (lower + if (lower < 10) 48 else 65 - 10).toChar()
        }
        return String(result)
    }

    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class
    )
    private fun hmacSha1(value: String, key: String): String? {
        val type = "HmacSHA1"
        val secret = SecretKeySpec(key.toByteArray(), type)
        val mac = Mac.getInstance(type)
        mac.init(secret)
        val bytes = mac.doFinal(value.toByteArray())
        return bytesToHex(bytes)
    }

    fun MD5(s: String): String? {
        val hexDigits = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f'
        )
        return try {
            //把字符串转换成字节码的形式
            val strTemp = s.toByteArray()
            //申明mdTemp为MD5加密的形式
            val mdTemp: MessageDigest = MessageDigest.getInstance("MD5")
            //进行字节加密并行进加密 转化成16位字节码的形式
            mdTemp.update(strTemp)
            val md: ByteArray = mdTemp.digest()
            //j=32
            val j = md.size
            val str = CharArray(j * 2)
            var k = 0
            //对字符串进行重新编码成32位的形式
            for (i in 0 until j) {
                val byte0 = md[i]
                str[k++] = hexDigits[byte0.toInt() ushr 4 and 0xf]
                str[k++] = hexDigits[byte0.toInt() and 0xf]
            }
            String(str)
        } catch (e: NoSuchAlgorithmException) {
            null
        }
    }

//    private val hexArray = "0123456789abcdef".toCharArray()
//
//    private fun bytesToHex(bytes: ByteArray): String? {
//        val hexChars = CharArray(bytes.size * 2)
//        var v: Int
//        for (j in bytes.indices) {
//            v = bytes[j] and 0xFF
//            hexChars[j * 2] = hexArray[v ushr 4]
//            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
//        }
//        return String(hexChars)
//    }
}