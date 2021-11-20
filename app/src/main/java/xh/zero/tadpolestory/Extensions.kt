package xh.zero.tadpolestory

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import xh.zero.core.network.RemoteRequestStrategy
import xh.zero.core.utils.ToastUtil
import xh.zero.core.vo.ApiResponse
import xh.zero.core.vo.Resource
import xh.zero.core.vo.Status
import xh.zero.core.widgets.NetworkStateLayout
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.repo.data.BaseData
import java.lang.Exception
import java.math.BigDecimal
import kotlin.math.roundToInt

/**
 * 网络请求策略
 *
 * 对于Repository里面的load方法, 可以写成下面的形式
 * ```
 * fun loadFunction() = networkRequestStrategy { function() : LiveData<ApiResponse<T>> }
 * ```
 */
inline fun <T> Repository.remoteRequestStrategy(
    isShowToast: Boolean = true,
    crossinline f: () -> LiveData<ApiResponse<T>>
) = object : RemoteRequestStrategy<T>(this.context) {
    override fun createCall(): LiveData<ApiResponse<T>> = f()

    override fun onFetchFailed(context: Context, error: String) {
        if (isShowToast) {
//            ToastUtil.showToast(context, error)
        }
    }
}.asLiveData()
//
///**
// * 缓存请求策略
// */
//inline fun <T> Repository.cacheRequestStrategy(
//    showToast: Boolean = true,
//    crossinline saveToDB: (data: T?) -> Unit,
//    crossinline getFromDB: () -> LiveData<T>,
//    crossinline f: () -> LiveData<ApiResponse<T>>
//) = object : CacheRequestStrategy<T>(this.context, this.appExecutors) {
//
//    override fun saveCallResult(result: T?) = saveToDB(result)
//
//    override fun loadFromDb(): LiveData<T> = getFromDB()
//
//    // 每次请求都需要网络数据
//    override fun shouldFetch(data: T?): Boolean = true
//
//    override fun createCall(): LiveData<ApiResponse<T>> = f()
//
//    override fun onFetchFailed(context: Context, error: String) {
//        // 请求失败时的提示
//        if (showToast) {
//            ToastUtil.show(context, error)
//        }
//    }
//
//    override fun onResponse(response: ApiResponse<T>) {
//    }
//}.asLiveData()

/**
 * api响应处理，用于remoteRequestStrategy和cacheRequestStrategy
 * 用于cacheRequestStrategy时会忽略缓存数据，效果和remoteRequestStrategy一样
 *
 * refreshLayout: 下拉刷新布局
 * networkLayout: 网络加载布局
 * isEmpty: 网络加载布局 -> 空状态判断
 * progressDialog: 模态加载中进度Dialog
 * context: 用来Toast提示code不为0的情况
 * loading: 扩展回调
 * error: 扩展回调
 */
inline fun <reified T : BaseData> handleResponse(
    resource: Resource<T>,
    refreshLayout: SmartRefreshLayout? = null,
    networkLayout: NetworkStateLayout? = null,
    isEmpty: (data: T?) -> Boolean? = { false },
    context: Context? = null,
    progressDialog: AlertDialog? = null,
    onLoading: () -> Unit = {},
    onError: () -> Unit = {},
    onDataError: (e: String) -> Unit = {},
    onSuccess: (T) -> Unit
) {
    networkLayout?.networkStatus(resource.status)
    if (resource.status == Status.SUCCESS) {
        if (resource.data?.code == 200) {
            refreshLayout?.finishRefresh(true)
            // networkStateLayout空状态
            val empty = isEmpty(resource.data)
            if (empty == null || empty) {
                networkLayout?.empty()
            } else {
                // 返回的结果有效
                resource.data.also { onSuccess(it!!) }
            }
        } else if (resource.data?.code == 2042) {
//            if (context != null) {
//                ToastUtil.showToast(context, resource.data?.msg ?: "")
//                LoginActivity.startWithNewTask(context)
//            }
        } else {
            onDataError(resource.data?.msg.orEmpty())
            networkLayout?.error()
            refreshLayout?.finishRefresh(false)
            if (context != null) {
                ToastUtil.show(context, resource.data?.msg ?: "")
            }
        }
        resource.data?.also { onSuccess(it) }
        refreshLayout?.finishRefresh(true)
        // networkStateLayout空状态
        val empty = isEmpty(resource.data)
        if (empty == null || empty) {
            networkLayout?.empty()
        }

        progressDialog?.dismiss()
    } else if (resource.status == Status.LOADING) {
        progressDialog?.show()
        onLoading()
    } else if (resource.status == Status.ERROR) {
        refreshLayout?.finishRefresh(false)
        networkLayout?.error()
        progressDialog?.dismiss()
        onError()
    }
}
//
///**
// * 缓存请求结果处理，用于cacheRequestStrategy
// * loading状态会有缓存数据传递，不需要加载过程
// */
//inline fun <reified T : BaseData> handleCacheResponse(
//    resource: Resource<T>,
//    context: Context? = null,
//    error: () -> Unit = {},
//    success: (T) -> Unit
//) {
//    if (resource.status == Status.SUCCESS) {
//        // 网络数据，加载成功传递给页面，如果缓存数据有修改，这里可以及时刷新
//        if (resource.data?.code == 0) {
//            resource.data?.also {
//                success(it)
//            }
//        } else {
//            error()
//            context?.also { ToastUtil.show(context, resource.data?.message) }
//        }
//    } else if (resource.status == Status.LOADING) {
//        // 缓存数据，在loading状态时传递给页面
//        if (resource.data?.code == 0) {
//            resource.data?.also {
//                success(it)
//            }
//        }
//    } else if (resource.status == Status.ERROR) {
//        error()
//    }
//}

fun <T> Fragment.startPlainActivity(target: Class<T>) where T : AppCompatActivity {
    startActivity(Intent(context, target))
}

fun <T> AppCompatActivity.startPlainActivity(target: Class<T>) where T : AppCompatActivity {
    startActivity(Intent(this, target))
}

// 金额处理，分转元
fun Int?.toYuan() : String {
    if (this == null) return ""
    return if (this % 100 == 0) {
        (this / 100).toString()
    } else {
        (this.toDouble() / 100).toString()
    }
}

// 金额处理，分转元，保留两位小数
//fun Int.toYuan(): String {
//    val mod = this % 100
//    val modStr = if (mod < 10) "0$mod" else "$mod"
//    val a = this / 100
//    return "$a.$modStr"
//}

fun Int.toKm(): String =
    BigDecimal(this.toDouble() / 1000).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString()

// 金额处理，元转分
fun Double.toCent() = (this * 100).toInt()
fun Float.toCent() = (this * 100).toInt()
fun Int.toCent() = this * 100

fun Int?.spec() : String {
    if (this == null) return ""
    return if (this % 100 == 0) {
        (this / 100).toString()
    } else {
        (this.toDouble() / 100).toString()
    }
}

fun String.toCent() : Int {
    try {
        val value = this.toDouble()
        return (value * 100).toInt()
    } catch (e: Exception) {
        return 0
    }
}

fun Float.toCount() : String {
    if ((this * 1000).roundToInt() % 1000 == 0) {
        return this.toInt().toString()
    } else {
        return this.toString()
    }
}

fun <T> HashSet<T>.toArrayList(): ArrayList<T> {
    val arr = ArrayList<T>()
    this.forEach { t ->
        arr.add(t)
    }
    return arr
}

fun String?.orStub(): String = this ?: "-"

fun Int?.orStub(): String = this?.toString() ?: "-"



