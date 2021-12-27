package xh.zero.tadpolestory.update

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import xh.zero.tadpolestory.BuildConfig
import java.io.*
import java.lang.Exception

/**
 * apk下载服务
 */
class DownloadService : Service() {
    companion object {
        private const val TAG = "DownloadService"
        private const val NOTIFICATION_ID = 2
        private const val EXTRA_UPDATE_URL = "com.ubtrobot.smartprojector.update.EXTRA_UPDATE_URL"
        private const val EXTRA_IS_FORCE = "com.ubtrobot.smartprojector.update.EXTRA_IS_FORCE"
        private const val CHANNEL_ID = "download_channel"
        private const val BUFFER_SIZE = 10 * 1024 // 8k ~ 32K

        var isDownloadCompleted = true

        fun startService(context: Activity, connection: ServiceConnection, url: String?, isForce: Boolean?) {
            if (url == null) return

            Intent(context, DownloadService::class.java).also { intent ->
                intent.putExtra(EXTRA_UPDATE_URL, url)
                intent.putExtra(EXTRA_IS_FORCE, isForce)
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }

        fun stopService(context: Context, connection: ServiceConnection) {
            context.unbindService(connection)
        }
    }

    private val binder = DownloadBinder()

    private var mCallback: DownloadCallback? = null

    private var notifyManager: NotificationManager? = null
    private lateinit var mBuilder: NotificationCompat.Builder
    private var downloadUrl: String? = null
    var reInstallApk: File? = null
    var isForceUpdate: Boolean? = false

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Timber.d("onBind")
        downloadUrl = intent?.getStringExtra(EXTRA_UPDATE_URL)
        isForceUpdate = intent?.getBooleanExtra(EXTRA_IS_FORCE, false)
        notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)

        val appName = getString(applicationInfo.labelRes)
        val icon = applicationInfo.icon

        mBuilder.setContentTitle(appName)
            .setSmallIcon(icon)
            .setOnlyAlertOnce(true)
            .priority = NotificationCompat.PRIORITY_LOW

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = "app update task"
            val description = "app download"
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = description
            // Register the channel with the system
            notifyManager?.createNotificationChannel(channel)
        }

        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }

    fun setDownloadCallback(callback: DownloadCallback) {
        mCallback = callback
    }

    fun downloadApk() {
        val url: String? = downloadUrl
        if (url == null) return

        val dir = getCacheDirectory(this)
        val queryIndex = url.indexOf("?")
        val endPos = if (queryIndex == -1) url.length else queryIndex
        val apkName = url.substring(url.lastIndexOf("/") + 1, endPos)
//        val apkName = url.substring(url.lastIndexOf("/") + 1, url.length)
        val apk = File(dir, apkName)

        mCallback?.onPrepare()

        Thread {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url)
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .build()

                val response = client.newCall(request).execute()
                inputStream = response.body?.byteStream()
                outputStream = FileOutputStream(apk)
                val totalBytes = response.body?.contentLength() ?: 0L

                val buffer = ByteArray(BUFFER_SIZE)
                var byteRead = inputStream?.read(buffer) ?: 0
                var byteSum = 0L
                var oldProgress = 0
                while (byteRead != -1) {
                    byteSum += byteRead
                    outputStream.write(buffer, 0, byteRead)

                    val progress = Math.round(byteSum / totalBytes.toDouble() * 100.0).toInt()
                    if (oldProgress != progress) {
                        onDownloadProgress(progress)
                    }
                    oldProgress = progress

                    byteRead = inputStream?.read(buffer) ?: 0
                }
                response.body?.close()
                onDownloadCompleted(apk)
            } catch (e: Exception) {
                onDownloadError(e.localizedMessage)
            } finally {
                inputStream?.close()
                outputStream?.close()
            }

        }.start()
    }

    private fun onDownloadProgress(percent: Int) {
        mCallback?.onProgress(percent)

        mBuilder.setContentText("正在下载$percent%")
            .setProgress(100, percent, false)
        notifyManager?.notify(NOTIFICATION_ID, mBuilder.build())
        isDownloadCompleted = false
    }

    private fun onDownloadCompleted(apk: File) {
        mCallback?.onComplete()

        // 保留Notification的progress，这里移除progress会出现bug
        mBuilder.setContentText("下载完成")
        notifyManager?.notify(NOTIFICATION_ID, mBuilder.build())

        installApk(apk)
        isDownloadCompleted = true
    }

    private fun onDownloadError(error: String?) {
        mCallback?.onError(error)

        mBuilder.setContentText("下载失败，请重新下载")
        notifyManager?.notify(NOTIFICATION_ID, mBuilder.build())
        isDownloadCompleted = true
    }

    fun installApk(apk: File) {
        reInstallApk = apk

        val intent = Intent(Intent.ACTION_VIEW)
        //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
        try {
            val command = arrayOf("chmod", "777", apk.toString())
            val builder = ProcessBuilder(*command)
            builder.start()

            var uri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", apk)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                uri = Uri.fromFile(apk)
            }

            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            mBuilder.setContentIntent(pendingIntent)
            notifyManager?.notify(NOTIFICATION_ID, mBuilder.build())

        } catch (ignored: IOException) {
            Log.e(TAG, ignored.localizedMessage)
        }
    }

    private fun getCacheDirectory(context: Context): File {
        var appCacheDir: File? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context)
        }
        if (appCacheDir == null) {
            //7.0以上如果不给外部存储卡读写权限，那么app下载到内部files目录下，FileProvider默认内部共享files目录
            appCacheDir = File(context.cacheDir, "ApkFiles")
            if (!appCacheDir.exists()) {
                appCacheDir.mkdir()
            }
        }
        if (appCacheDir == null) {
            Log.w("StorageUtils", "Can't define system cache directory! The app should be re-installed.")
        }
        return appCacheDir
    }

    private fun getExternalCacheDir(context: Context): File? {
        val appCacheDir = File(context.externalCacheDir, "ApkFiles")
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w("StorageUtils", "Unable to create external cache directory")
                return null
            }
            try {
                File(appCacheDir, ".nomedia").createNewFile()
            } catch (e: IOException) {
                Log.i("StorageUtils", "Can't create \".nomedia\" file in application external cache directory")
            }

        }
        return appCacheDir
    }

    private fun hasExternalStoragePermission(context: Context): Boolean {
        val perm = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE")
        return perm == PackageManager.PERMISSION_GRANTED
    }

    inner class DownloadBinder : Binder() {
        fun getService() : DownloadService = this@DownloadService
    }

    interface DownloadCallback {
        fun onPrepare()
        fun onProgress(progress: Int)
        fun onComplete()
        fun onError(error: String?)
    }

}