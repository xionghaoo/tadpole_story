package xh.zero.tadpolestory.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.lzf.easyfloat.EasyFloat
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.startPlainActivity
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.GlideApp
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityMainBinding
import xh.zero.tadpolestory.replaceFragment
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.repo.TadpoleMusicService
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.ui.album.*
import xh.zero.tadpolestory.ui.home.ChildStoryFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity(),
    ChildStoryFragment.OnFragmentActionListener,
    TrackListFragment.OnFragmentActionListener
{

    companion object {
        const val ACTION_NOTIFICATION_PLAYER = "${Configs.PACKAGE_NAME}.MainActivity.ACTION_NOTIFICATION_PLAYER"
        const val ACTION_ALBUM_DETAIL = "${Configs.PACKAGE_NAME}.MainActivity.ACTION_ALBUM_DETAIL"

        fun startToAlbumDetail(context: Context?, item: Album) {
            context?.startActivity(Intent(context, MainActivity::class.java).apply {
                action = ACTION_ALBUM_DETAIL
                putExtra("album", item)
            })
        }
    }

    private lateinit var binding: ActivityMainBinding
    private var startX: Float = 0f
    private var expandStartX: Float = 0f

    private val viewModel: NowPlayingViewModel by viewModels()

    private var isCollapse: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        overridePendingTransition(R.anim.page_enter, 0)

        startNowPlayingPage(intent)

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
                    initialFloatWindow(view)
                }
                touchEvent { view, e ->
                }
            }
            .show()

        attachKeyboardListeners()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        startNowPlayingPage(intent)

        if (intent?.action == ACTION_ALBUM_DETAIL) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.albumDetailFragment, Bundle().apply {
                putParcelable("album", intent.getParcelableExtra("album"))

            }, navOptions {
                anim {
                    enter = R.anim.page_enter
                    popExit = R.anim.page_exit
                }
                launchSingleTop = true
            })
        }
    }

    override fun onDestroy() {
        EasyFloat.dismiss("float_window")
        super.onDestroy()
    }

    private fun startNowPlayingPage(intent: Intent?) {
        if (intent?.action == ACTION_NOTIFICATION_PLAYER) {
            NowPlayingActivity.start(this, viewModel.repo.prefs.nowPlayingAlbumTitle)
        }
    }

    private fun initialFloatWindow(view: View?) {
        if (view == null) return
        val floatRoot = view.findViewById<View>(R.id.float_root)
        val viewExpand = view.findViewById<View>(R.id.float_player_view_expand)
        val viewCollapse = view.findViewById<CardView>(R.id.float_player_view_collapse)

        val ivCoverStep1 = view.findViewById<ImageView>(R.id.iv_float_cover_step_1)
        val ivCoverStep2 = view.findViewById<ImageView>(R.id.iv_float_cover_step_2)
        val expandBtnPlayer = view.findViewById<ImageView>(R.id.expand_btn_player)
        val collapseBtnPlayer = view.findViewById<ImageView>(R.id.collapse_btn_player)
        val expandTitle = view.findViewById<TextView>(R.id.expand_tv_title)
        val expandDesc = view.findViewById<TextView>(R.id.expand_tv_desc)
        val expandProgressBar = view.findViewById<CircularProgressBar>(R.id.expand_progress_bar)
        val collapseProgressBar = view.findViewById<CircularProgressBar>(R.id.collapse_progress_bar)

        EasyFloat.getFloatView("float_window")?.visibility = View.GONE
        viewModel.mediaMetadata.observe(this) { mediaItem ->
            EasyFloat.getFloatView("float_window")?.visibility = if (viewModel.repo.prefs.nowPlayingAlbumId != null) View.VISIBLE else View.GONE
            expandTitle.text = mediaItem.title
            expandDesc.text = mediaItem.subtitle

            GlideApp.with(view.context)
                .load(mediaItem.albumArtUri)
                .circleCrop()
                .into(ivCoverStep1)

            GlideApp.with(view.context)
                .load(mediaItem.albumArtUri)
                .circleCrop()
                .into(ivCoverStep2)
        }

        // 播放按钮
        viewModel.mediaButtonRes.observe(this) { res ->
            expandBtnPlayer.setImageResource(res)
            collapseBtnPlayer.setImageResource(res)
        }

        expandBtnPlayer.setOnClickListener {
            viewModel.mediaMetadata.value?.let {
                viewModel.playMediaId(it.id)
            }
        }
        collapseBtnPlayer.setOnClickListener {
            viewModel.mediaMetadata.value?.let {
                viewModel.playMediaId(it.id)
            }
        }

        // 播放位置
        viewModel.mediaProgress.observe(this) { progress ->
//            if (!floatRoot.isVisible) floatRoot.visibility = View.VISIBLE
            collapseProgressBar.progress = progress.toFloat()
            expandProgressBar.progress = progress.toFloat()

            ivCoverStep1.animate()
                .rotationBy(2f)
                .start()
            ivCoverStep2.animate()
                .rotationBy(2f)
                .start()
        }

        val lp = viewCollapse?.layoutParams as FrameLayout.LayoutParams
        lp.marginEnd = -resources.getDimension(R.dimen._60dp).toInt()
        val viewBackground = view.findViewById<CardView>(R.id.expand_root)
        if (Build.VERSION.SDK_INT >= 28) {
            viewBackground?.outlineAmbientShadowColor =
                ContextCompat.getColor(this@MainActivity, R.color.colorAccent)
            viewBackground?.outlineSpotShadowColor =
                ContextCompat.getColor(this@MainActivity, R.color.colorAccent)
        }

        viewCollapse?.setOnTouchListener { v, e ->
            val viewCollapse = view.findViewById<CardView>(R.id.float_player_view_collapse)
            if (!viewCollapse.isVisible) return@setOnTouchListener true
            val collapseExpandSpace = view.findViewById<View>(R.id.collapse_extra_space)
//            Timber.d("${e.x}, ${e.y}")
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
//                    Timber.d("translationX: ${viewCollapse.translationX}")
                    if (viewCollapse.translationX <= -resources.getDimension(R.dimen._42dp)) {
                        // 拖拽结束
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
                    if (diff >= 20) {
                        hideFloatWindow()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (e.x - expandStartX < 20) {
                        NowPlayingActivity.start(this, viewModel.repo.prefs.nowPlayingAlbumTitle)
                    }
                }
            }
            return@setOnTouchListener true
        }
    }

    override fun hideFloatWindow() {
        showFloatPlayer(false, EasyFloat.getFloatView("float_window"))
    }

    private fun showFloatPlayer(isExpand: Boolean, view: View?) {
        if (view == null) return
        val viewExpand = view.findViewById<View>(R.id.float_player_view_expand)
        val viewCollapse = view.findViewById<View>(R.id.float_player_view_collapse)
        val viewBackground = view.findViewById<CardView>(R.id.expand_root)
        val collapseExpandSpace = view.findViewById<View>(R.id.collapse_extra_space)

        if (isExpand) {
            isCollapse = false
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
                        }
                        .start()
                }
                .start()
        } else {
            if (isCollapse == true) {
                return
            }
            isCollapse = true
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

                                    // TODO 动画存在bug
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