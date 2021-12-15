package com.example.android.uamp.media

import android.content.Context
import android.util.Log
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.internal.addHeaderLenient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 音频缓存请求
 */
class CacheDataSourceFactory(private val context: Context,
                             private val maxCacheSize: Long,
                             private val maxFileSize: Long) : DataSource.Factory {

    private val defaultDatasourceFactory: DefaultDataSource.Factory
    private val simpleCache: SimpleCache by lazy {
        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
        SimpleCache(File(context.cacheDir, "media"), evictor)
    }

    init {
        val userAgent = Util.getUserAgent(context, context.packageName)
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context)
        /**
         * bandwidthMeter,
        DefaultHttpDataSource.Factory().apply {
        setUserAgent(userAgent)

        }),
         */
        // 音频http请求
//        defaultDatasourceFactory = DefaultHttpDataSource.Factory().apply {
//            setAllowCrossProtocolRedirects(true)
//            setUserAgent(userAgent)
//            setConnectTimeoutMs(10_000)
//        }
        val client = OkHttpClient.Builder()
            .connectTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val newRequestBuilder = request.newBuilder()
                    .removeHeader("Range")
                    .addHeader("Range", "bytes=0-")
//                    .addHeader("Connection", "keep-alive")
//                    .addHeader("Cache-Control", "no-cache")
//                    .addHeader("Pragma", "no-cache")
                val newRequest = newRequestBuilder.build()
                Log.d("CacheDataSourceFactory", "------> ${newRequest.url}")
                Log.d("CacheDataSourceFactory", "${newRequest.headers}")
                val response = chain.proceed(newRequest)
                Log.d("CacheDataSourceFactory", "<------${response.code}")
                Log.d("CacheDataSourceFactory", "${response.headers}")
                return@addInterceptor response
            }
            .build()

        defaultDatasourceFactory = DefaultDataSource.Factory(
            context,
            OkHttpDataSource.Factory(client).apply {
                setUserAgent(userAgent)
            }
//            DefaultHttpDataSource.Factory()
        )
    }

    override fun createDataSource(): DataSource {
        return CacheDataSource(
            simpleCache,
            defaultDatasourceFactory.createDataSource(),
            FileDataSource(),
            CacheDataSink(simpleCache, maxFileSize),
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
            null
        )
//        return defaultDatasourceFactory.createDataSource()
    }
}