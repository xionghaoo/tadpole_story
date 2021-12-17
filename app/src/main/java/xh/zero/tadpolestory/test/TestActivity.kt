package xh.zero.tadpolestory.test

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.startPlainActivity
import xh.zero.core.vo.Status
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnMedia.setOnClickListener {
            showTopView()
        }
        binding.btnLoad.setOnClickListener {
            startPlainActivity(TestActivity::class.java)
        }
        android.R.attr.activityOpenEnterAnimation
    }

    private fun showPopWindow() {
        val popupWindow = PopupWindow(300, 300)
        val container = LayoutInflater.from(this).inflate(R.layout.test_view, null)
        popupWindow.contentView = container
//        val lp = container.layoutParams
//        lp.width = 300
//        lp.height = 300
        popupWindow.windowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        popupWindow.showAtLocation(window.decorView, Gravity.LEFT, 0, 0)
    }

    private fun showTopView() {
        val windowManager =
            applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.START or Gravity.TOP
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
        layoutParams.token = window.decorView.windowToken // 必须要

        val view = LayoutInflater.from(this).inflate(R.layout.test_view, null)
        windowManager.addView(view, layoutParams)
    }
}