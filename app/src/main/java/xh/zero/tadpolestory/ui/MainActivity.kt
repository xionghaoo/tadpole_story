package xh.zero.tadpolestory.ui

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.lzf.easyfloat.EasyFloat
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : BaseActivity(), MainFragment.OnFragmentActionListener {

    companion object {
//        const val ACTION_HIDE_FLOAT_WINDOW = "${Configs.PACKAGE_NAME}.MainActivity.ACTION_HIDE_FLOAT_WINDOW"
    }

    private lateinit var binding: ActivityMainBinding
    private var startX: Float = 0f
    private var expandStartX: Float = 0f
//    private var isDragFinish = false
//    private var isFloatWindowExpand = true
    private var lastExpandState: Boolean? = null

//    private val receiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == ACTION_HIDE_FLOAT_WINDOW) {
//                Timber.d("scroll hide:")
//
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        registerReceiver(receiver, IntentFilter(ACTION_HIDE_FLOAT_WINDOW))

        EasyFloat.with(this)
            .setDragEnable(false)
            .setGravity(
                Gravity.END or Gravity.BOTTOM,
                0,
                offsetY = -resources.getDimension(R.dimen._34dp).toInt()
            )
            .setTag("float_window")
            .setLayout(R.layout.float_player_view)
            .registerCallback {
                createResult { b, s, view ->
                    val viewExpand = view?.findViewById<View>(R.id.float_player_view_expand)
                    val viewCollapse = view?.findViewById<CardView>(R.id.float_player_view_collapse)
                    val lp = viewCollapse?.layoutParams as FrameLayout.LayoutParams
                    lp.marginEnd = -resources.getDimension(R.dimen._60dp).toInt()
                    val viewBackground = view.findViewById<CardView>(R.id.v_float_root)
                    if (Build.VERSION.SDK_INT >= 28) {
                        viewBackground?.outlineAmbientShadowColor =
                            ContextCompat.getColor(this@MainActivity, R.color.colorAccent)
                        viewBackground?.outlineSpotShadowColor =
                            ContextCompat.getColor(this@MainActivity, R.color.colorAccent)
                    }
//                    viewExpand?.setOnClickListener {
////                        ToastUtil.show(this@MainActivity, "点击测试")
////                        isShow = isShow.not()
//                        isDragFinish = true
//                        showFloatPlayer(false, view)
//                    }

                    viewCollapse?.setOnTouchListener { v, e ->
                        val viewCollapse = view.findViewById<CardView>(R.id.float_player_view_collapse)
                        if (!viewCollapse.isVisible) return@setOnTouchListener true
                        val collapseExpandSpace = view.findViewById<View>(R.id.collapse_extra_space)
//                        collapseExpandSpace.pivotX = collapseExpandSpace.right.toFloat()
                        Timber.d("${e.x}, ${e.y}")
                        when (e.action) {
                            MotionEvent.ACTION_DOWN -> {
                                viewCollapse.elevation = 0f
                                startX = e.x
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val diff = startX - e.x
                                if (diff > 10) {
                                    collapseExpandSpace.visibility = View.INVISIBLE
                                    viewCollapse.translationX = if (diff > 0) - diff else 0f
                                    if (viewCollapse.translationX <= -resources.getDimension(R.dimen._42dp)) {
                                        viewCollapse.translationX = -resources.getDimension(R.dimen._42dp)
                                    }
                                }
                            }
                            MotionEvent.ACTION_UP -> {
                                Timber.d("translationX: ${viewCollapse.translationX}")
                                if (viewCollapse.translationX <= -resources.getDimension(R.dimen._42dp)) {
                                    ToastUtil.show(this@MainActivity, "拖拽完成")
//                                    isDragFinish = true
                                    showFloatPlayer(true, view)
                                } else {
                                    viewCollapse.animate()
                                        .translationX(0f)
                                        .withEndAction {
                                            collapseExpandSpace.visibility = View.VISIBLE
                                            viewCollapse?.elevation = resources.getDimension(R.dimen._4dp)
                                        }
                                        .start()
                                }
                            }
                        }
                        return@setOnTouchListener true
                    }

                    viewExpand?.setOnTouchListener { v, e ->
                        val viewExpand = view?.findViewById<View>(R.id.float_player_view_expand)
                        if (!viewExpand.isVisible) return@setOnTouchListener true
                        when (e.action) {
                            MotionEvent.ACTION_DOWN -> {
                                expandStartX = e.x
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val diff = e.x - expandStartX
                                if (diff > 10) {
                                    hideFloatWindow()
                                }
                            }
                        }
                        return@setOnTouchListener true
                    }
                }
                touchEvent { view, e ->
                }
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun hideFloatWindow() {
        showFloatPlayer(false, EasyFloat.getFloatView("float_window"))
    }

    private fun showFloatPlayer(isExpand: Boolean, view: View?) {

        if (view == null) return
        val viewExpand = view.findViewById<View>(R.id.float_player_view_expand)
        val viewCollapse = view.findViewById<View>(R.id.float_player_view_collapse)
        val viewBackground = view.findViewById<CardView>(R.id.v_float_root)
        val collapseExpandSpace = view.findViewById<View>(R.id.collapse_extra_space)
//        Timber.d("showFloatPlayer: ${lastExpandState}, $isExpand")

        if (lastExpandState == isExpand) return
        lastExpandState = isExpand
        if (isExpand) {
            Timber.d("showFloatPlayer: 展开")
            // 展开
            viewBackground.visibility = View.VISIBLE
            viewCollapse?.visibility = View.INVISIBLE

            viewExpand.animate()
                .translationX(0f)
                .withEndAction {
                    viewBackground.animate()
                        .translationZ(resources.getDimension(R.dimen._4dp))
                        .withEndAction {
//                            isDragFinish = false
                        }
                        .start()
                }
                .start()
        } else {
            Timber.d("showFloatPlayer: 收起")
            // 收起
            viewBackground.animate()
                .translationZ(0f)
                .withEndAction {
                    viewBackground.elevation = 0f
                    viewExpand.animate()
                        .translationX(viewExpand.width.toFloat() - viewExpand.height)
                        .withEndAction {
                            // 把视图替换掉
                            viewBackground.animate()
                                .translationX(resources.getDimension(R.dimen._42dp))
                                .withEndAction {
                                    viewBackground.visibility = View.INVISIBLE
                                    viewBackground.translationX = 0f
                                    viewCollapse?.translationX = 0f
                                    viewCollapse?.visibility = View.VISIBLE
                                    collapseExpandSpace?.visibility = View.VISIBLE

                                    viewCollapse.elevation = resources.getDimension(R.dimen._4dp)

                                    // 动画存在bug
//                                    viewCollapse.animate()
//                                        .translationZ(resources.getDimension(R.dimen._4dp))
//                                        .withEndAction {
//                                            viewCollapse.translationZ = 0f
//                                            viewCollapse.elevation = resources.getDimension(R.dimen._4dp)
////                                            isDragFinish = false
//                                        }
//                                        .start()
                                }
                                .start()
                        }
                        .setDuration(300)
                        .start()
                }
                .start()
        }

    }

}