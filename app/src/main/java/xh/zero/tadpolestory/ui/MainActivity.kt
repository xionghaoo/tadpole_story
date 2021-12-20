package xh.zero.tadpolestory.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.lzf.easyfloat.EasyFloat
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.GlideApp
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityMainBinding
import xh.zero.tadpolestory.repo.Repository
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
        const val ACTION_UPLOAD_PLAY_RECORD = "${Configs.PACKAGE_NAME}.MainActivity.ACTION_UPLOAD_PLAY_RECORD"
        private const val RC_READ_PHONE_STATE_PERMISSION = 1

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
    @Inject
    lateinit var repo: Repository

    private var isCollapse: Boolean? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_UPLOAD_PLAY_RECORD -> {
                }
            }
        }
    }

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

        val filter = IntentFilter(ACTION_UPLOAD_PLAY_RECORD)
        registerReceiver(receiver, filter)

        getSerialNumberTask()
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
        unregisterReceiver(receiver)
        EasyFloat.dismiss("float_window")
        super.onDestroy()
    }

    private fun startNowPlayingPage(intent: Intent?, isFloatWindow: Boolean = false) {
        if (intent?.action == ACTION_NOTIFICATION_PLAYER || isFloatWindow ) {
            NowPlayingActivity.start(this, viewModel.repo.prefs.nowPlayingAlbumTitle, viewModel.repo.prefs.nowPlayingAlbumId ?: "")
        }
    }

    private fun initialFloatWindow(view: View?) {
        if (view == null) return
//        val vTrackInfo = view.findViewById<View>(R.id.container_track_info)
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
        expandProgressBar.progressMax = NowPlayingFragment.MAX_PROGRESS.toFloat()
        collapseProgressBar.progressMax = NowPlayingFragment.MAX_PROGRESS.toFloat()

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

            // 上报播放记录
            if (!viewModel.isPlaying) {
                viewModel.uploadRecords(mediaItem)
            }
        }

//        vTrackInfo.setOnClickListener {
//            startNowPlayingPage(null, true)
//        }

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

        // 悬浮窗展开事件监听
        viewExpand?.setOnTouchListener { v, e ->
            val viewExpand = view?.findViewById<View>(R.id.float_player_view_expand)
            if (!viewExpand.isVisible) return@setOnTouchListener true
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    expandStartX = e.x
                }
                MotionEvent.ACTION_MOVE -> {
                    /**
                     * 向右滑动收起悬浮窗
                     */
                    val diff = e.x - expandStartX
                    if (diff >= 30) {
                        hideFloatWindow()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    /**
                     * 点击事件，去掉播放器的部分 60(marginEnd) + 80 = 140
                     */
                    if (e.x - expandStartX < 10 && e.x < viewExpand.width - resources.getDimension(R.dimen._140dp)) {
                        startNowPlayingPage(null, true)
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

    // ------------------- 获取本机序列号 ------------------------
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(RC_READ_PHONE_STATE_PERMISSION)
    fun getSerialNumberTask() {
        if (repo.prefs.serialNumber == null) {
            repo.prefs.serialNumber = Build.ID ?: "tadpole_${(0..100).random()}"
        }
        if (hasReadPhoneStatePermission()) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                val serialNo = Build.getSerial()
                Timber.d("本机序列号：${serialNo}")
                // 设备序列必须大于8位
                repo.prefs.serialNumber = if (serialNo.length <= 7) "${serialNo}00000000" else serialNo
            } else {
//                ToastUtil.showToast(this, "获取序列号失败")
                Timber.e("获取序列号失败")
            }
            Timber.d("perfs.serialNumber = ${repo.prefs.serialNumber}")

//            handler.postDelayed({
//                if (perfs.userID != -1) {
//                    startPlainActivity(MainActivity::class.java)
//                } else {
//                    startPlainActivity(LoginActivity::class.java)
//                }
//                finish()
//            }, 200)
        } else {
            EasyPermissions.requestPermissions(
                this,
                "App需要读取本机序列号，请授予权限",
                RC_READ_PHONE_STATE_PERMISSION,
                Manifest.permission.READ_PHONE_STATE
            )
        }
    }

    private fun hasReadPhoneStatePermission() : Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE)
    }

    // ------------------- 获取本机序列号 ------------------------

}