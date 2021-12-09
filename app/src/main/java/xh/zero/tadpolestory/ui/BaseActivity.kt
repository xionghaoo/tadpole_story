package xh.zero.tadpolestory.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber
import xh.zero.core.utils.SystemUtil

import android.content.Intent
import android.view.*

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import xh.zero.tadpolestory.R


open class BaseActivity : AppCompatActivity() {

    private val keyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (rootLayout != null) {
            val heightDiff = rootLayout!!.rootView.height - rootLayout!!.height
            val contentViewTop = window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
            val broadcastManager = LocalBroadcastManager.getInstance(this)
            if (heightDiff <= contentViewTop) {
                onHideKeyboard()
                val intent = Intent("KeyboardWillHide")
                broadcastManager.sendBroadcast(intent)
            } else {
                val keyboardHeight = heightDiff - contentViewTop
                onShowKeyboard(keyboardHeight)
                val intent = Intent("KeyboardWillShow")
                intent.putExtra("KeyboardHeight", keyboardHeight)
                broadcastManager.sendBroadcast(intent)
            }
        }
    }

    private var keyboardListenersAttached = false
    private var rootLayout: ViewGroup? = null

    protected open fun onShowKeyboard(keyboardHeight: Int) {
        Timber.d("onShowKeyboard")
    }
    protected open fun onHideKeyboard() {
        Timber.d("onHideKeyboard")
        val decor = window.decorView
        val flags = decor.systemUiVisibility or (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decor.systemUiVisibility = flags
    }

    protected open fun attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return
        }
        rootLayout = findViewById<View>(R.id.root_view) as? ViewGroup
        rootLayout?.viewTreeObserver?.addOnGlobalLayoutListener(keyboardLayoutListener)
        keyboardListenersAttached = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUtil.statusBarTransparent(window)
        SystemUtil.setDarkStatusBar(window)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (keyboardListenersAttached) {
            rootLayout?.viewTreeObserver?.removeGlobalOnLayoutListener(keyboardLayoutListener);
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Timber.d("onWindowFocusChanged: $hasFocus")
        if (hasFocus) {
            val decor = window.decorView
            val flags = decor.systemUiVisibility or (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            decor.systemUiVisibility = flags
        }
    }
}