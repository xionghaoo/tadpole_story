package xh.zero.tadpolestory.ui.album

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxItemDecoration
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.*
import timber.log.Timber
import xh.zero.core.utils.ImageUtil
import xh.zero.core.utils.SystemUtil
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.GlideApp
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentNowPlayingBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.ui.MainActivity
import xh.zero.tadpolestory.utils.OperationType
import xh.zero.tadpolestory.utils.PromptDialog
import kotlin.math.roundToInt
import kotlin.math.roundToLong

import androidx.core.content.ContextCompat

/**
 * 播放页面
 */
@AndroidEntryPoint
class NowPlayingFragment : BaseFragment<FragmentNowPlayingBinding>() {

    private val viewModel: NowPlayingViewModel by viewModels()
    private var isTouchingSeekBar = false

    private var hasInit = false
    private var topCoverImgX = 0f
    private var topCoverImgY = 0f
    private var coverImgX = 0f
    private var coverImgY = 0f

    private var topTvAlbumTitleX = 0f
    private var topTvAlbumTitleY = 0f
    private var tvAlbumTitleX = 0f
    private var tvAlbumTitleY = 0f

    private var topPbMediaProgressX = 0f
    private var topPbMediaProgressY = 0f
    private var pbMediaProgressX = 0f
    private var pbMediaProgressY = 0f

    private var progressBarScrollDiff = 0f
    private var relativeAlbumExtra1ScrollDiff = 0f

    private var currentPlayMediaId: String? = null
    private var currentScrollY: Int = 0

    private var selectedMultipleIndex = 2
    private var selectedTimingIndex = 0

    private val albumTitle: String by lazy {
        arguments?.getString(ARG_ALBUM_TITLE) ?: ""
    }

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentNowPlayingBinding {
        return FragmentNowPlayingBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.sendBroadcast(Intent(AlbumDetailFragment.ACTION_RECORD_ALBUM))

        viewModel.repo.prefs.nowPlayingAlbumTitle = albumTitle

        viewModel.mediaMetadata.observe(viewLifecycleOwner) { mediaItem ->
            updateUI(mediaItem)

            viewModel.uploadRecords(mediaItem)
        }

        viewModel.mediaPosition.observe(viewLifecycleOwner) { pos ->
            binding.tvMeidaPlayPosition.text = NowPlayingViewModel.NowPlayingMetadata.timestampToMSS(pos)
        }
        viewModel.mediaButtonRes.observe(viewLifecycleOwner) { res ->
            binding.btnMediaPlay.setImageResource(res)
            binding.topBtnMediaPlay.setImageResource(res)
        }

        initialProgressBar()

        binding.btnMediaPlay.setOnClickListener {
            viewModel.mediaMetadata.value?.let {
                viewModel.playMediaId(it.id)
            }
        }
        binding.topBtnMediaPlay.setOnClickListener {
            viewModel.mediaMetadata.value?.let {
                viewModel.playMediaId(it.id)
            }
        }

        binding.btnMediaPre.setOnClickListener {
            viewModel.prev()
        }

        binding.btnMediaNext.setOnClickListener {
            viewModel.next()
        }

        binding.btnMediaNext15s.setOnClickListener {
            viewModel.playForward15s()
        }
        binding.topBtnMediaNext15s.setOnClickListener {
            viewModel.playForward15s()
        }
        binding.btnMediaPrev15s.setOnClickListener {
            viewModel.playBackward15s()
        }

        binding.btnMediaMultiple.setOnClickListener {
            showMultiplePlayDialog()
        }
        binding.topBtnMediaMultiple.setOnClickListener {
            showMultiplePlayDialog()
        }
        binding.btnMediaTiming.setOnClickListener {
            showTimingPlay()
        }
        binding.btnMediaCatelog.setOnClickListener {
            // 跳转到曲目列表
            ToastUtil.show(context, "跳转到曲目列表")
            // TODO 交互存在问题
//            viewModel.repo.findCurrentAlbum { album ->
//                if (album != null) MainActivity.startToAlbumDetail(context, album)
//            }
        }
        binding.btnSubscribe.setOnClickListener {
            subscribe()
        }
        binding.topBtnSubscribe.setOnClickListener {
            subscribe()
        }

        // TODO 更多相关专辑
        binding.layoutRelativeAlbums.tvMediaRelativeMore.setOnClickListener {
            ToastUtil.show(context, "显示更多相关专辑")
        }
        binding.topTvMediaRelativeMore.setOnClickListener {
            ToastUtil.show(context, "显示更多相关专辑")
        }

        // 上一曲，下一曲按钮状态
        viewModel.switchState.observe(viewLifecycleOwner) {
            binding.btnMediaPre.setImageResource(
                if (it.first) {
                    binding.btnMediaPre.isEnabled = true
                    R.mipmap.ic_media_pre
                } else {
                    binding.btnMediaPre.isEnabled = false
                    R.mipmap.ic_media_pre_disable
                }
            )
            binding.btnMediaNext.setImageResource(
                if (it.second) {
                    binding.btnMediaNext.isEnabled = true
                    R.mipmap.ic_media_next
                } else {
                    binding.btnMediaNext.isEnabled = false
                    R.mipmap.ic_media_pre_disable
                }
            )
        }

        // 初始化View位置参数
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            if (!hasInit) {
                hasInit = true
                val target = binding.ivMediaCoverImg
                val origin = binding.topIvMediaCoverImg
                coverImgX = target.x
                coverImgY = binding.containerPlayer.y - SCROLL_THRESHOLD
                topCoverImgX = origin.x
                topCoverImgY = origin.y

                topTvAlbumTitleX = binding.topTvMediaAlbumTitle.x
                topTvAlbumTitleY = binding.topTvMediaAlbumTitle.y
                tvAlbumTitleX = binding.tvMediaAlbumTitle.x
                tvAlbumTitleY = binding.tvMediaAlbumTitle.y

                pbMediaProgressX = binding.pbMediaProgress.x
                pbMediaProgressY = binding.pbMediaProgress.y + binding.containerPlayer.y
                topPbMediaProgressX = binding.topPbMediaProgress.x
                topPbMediaProgressY = binding.topPbMediaProgress.y

                progressBarScrollDiff = binding.pbMediaProgress.y + binding.containerPlayer.y - binding.topPbMediaProgress.y
                relativeAlbumExtra1ScrollDiff = binding.layoutRelativeAlbums.tvMediaRelativeExtra1.y +
                        binding.layoutRelativeAlbums.root.y - binding.topTvMediaRelativeExtra1.y
            }
        }

        binding.scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            handleTransform(scrollY)
        }

        binding.scrollView.setOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP
                || event?.action == MotionEvent.ACTION_CANCEL) {
//                Timber.d("current scroll y = $currentScrollY, $progressBarScrollDiff, $relativeAlbumExtra1ScrollDiff")
                CoroutineScope(Dispatchers.Default).launch {
                    delay(100)
                    withContext(Dispatchers.Main) {
                        if (currentScrollY < progressBarScrollDiff / 3) {
                            binding.scrollView.smoothScrollTo(0, 0)
                        } else if (currentScrollY >= progressBarScrollDiff / 3 && currentScrollY < progressBarScrollDiff) {
                            binding.scrollView.smoothScrollTo(0, progressBarScrollDiff.toInt())
                        } else if (currentScrollY < progressBarScrollDiff + (relativeAlbumExtra1ScrollDiff - progressBarScrollDiff) / 3) {
                            binding.scrollView.smoothScrollTo(0, progressBarScrollDiff.toInt())
                        } else if (currentScrollY < relativeAlbumExtra1ScrollDiff) {
                            binding.scrollView.smoothScrollTo(0, relativeAlbumExtra1ScrollDiff.toInt())

                        }
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    /**
     * 渲染UI
     */
    private fun updateUI(mediaItem: NowPlayingViewModel.NowPlayingMetadata) {
        // 播放状态发生变化时，会触发mediaItem的变化
        if (currentPlayMediaId == mediaItem.id) return
        currentPlayMediaId = mediaItem.id
        binding.tvMediaDuration.text = NowPlayingViewModel.NowPlayingMetadata.timestampToMSS(mediaItem.duration)
        binding.tvMediaAlbumTitle.text = albumTitle
        binding.tvMediaTitle.text = mediaItem.title
        binding.tvMediaSubtitle.text = mediaItem.subtitle
        GlideApp.with(this)
            .load(mediaItem.albumArtUri)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(resources.getDimension(R.dimen._24dp).roundToInt())))
            .into(binding.ivMediaCoverImg)

        binding.topTvMediaAlbumTitle.text = albumTitle
        binding.topTvMediaTitle.text = mediaItem.title
        binding.topTvMediaSubtitle.text = mediaItem.subtitle
        val rate = resources.getDimension(R.dimen._140dp) / resources.getDimension(R.dimen._250dp)
        GlideApp.with(this)
            .load(mediaItem.albumArtUri)
            .apply(RequestOptions.bitmapTransform(RoundedCorners((resources.getDimension(R.dimen._24dp) * rate).roundToInt())))
            .into(binding.topIvMediaCoverImg)
        loadRelativeAlbum(mediaItem.id.toInt())

        // 加载背景
        CoroutineScope(Dispatchers.IO).launch {
            val img: Bitmap = GlideApp.with(requireContext()).asBitmap().load(mediaItem.albumArtUri).submit().get()
            // 给背景加上白色
            val paint = Paint()
            val filter: ColorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white_62),
                PorterDuff.Mode.SRC_IN
            )
            paint.colorFilter = filter
            val canvas = Canvas(img)
            canvas.drawBitmap(img, 0f, 0f, paint)

            withContext(Dispatchers.Main) {
                GlideApp.with(requireContext())
                    .load(img)
                    // 背景虚化
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(50)))
                    .into(binding.ivBackground)
            }
        }


    }

    /**
     * 加载相关专辑
     */
    private fun loadRelativeAlbum(trackId: Int) {
        viewModel.getRelativeAlbum(trackId).observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
                val container = binding.layoutRelativeAlbums.rcRelativeAlbums
                val itemDecoration = FlexboxItemDecoration(requireContext())
                itemDecoration.setOrientation(FlexboxItemDecoration.HORIZONTAL)
                container.removeAllViews()
                r.forEachIndexed { index, item ->
                    val v = LayoutInflater.from(requireContext()).inflate(R.layout.list_item_relative_album, null)
                    v.findViewById<TextView>(R.id.tv_album_title).text = item.album_title
                    v.findViewById<TextView>(R.id.tv_album_desc).text = item.album_intro
                    Glide.with(v.context)
                        .load(item.cover_url_middle)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(v.context.resources.getDimension(R.dimen._18dp).roundToInt())))
                        .into(v.findViewById<ImageView>(R.id.iv_album_icon))

                    v.setOnClickListener {
                        ToastUtil.show(requireContext(), "查看专辑详情")
                    }
                    container.addView(v)
                    // horizon = 16, vertical = 12
                    val lp = (v.layoutParams as FlexboxLayout.LayoutParams)
                    lp.width = resources.getDimension(R.dimen._160dp).toInt()
                    lp.height = resources.getDimension(R.dimen._224dp).toInt()
                    lp.topMargin = resources.getDimension(R.dimen._12dp).toInt()
                    lp.bottomMargin = resources.getDimension(R.dimen._12dp).toInt()
                    lp.marginStart = if (index % 6 == 0) 0 else resources.getDimension(R.dimen._16dp).toInt()

                    v.setOnClickListener {
                        // 相关专辑
                        MainActivity.startToAlbumDetail(context, item)
                        activity?.finish()
                    }
                }

                if (r.isNotEmpty()) {
                    // 填充不够滚动的空间
                    val display = SystemUtil.displayInfo(requireContext())
                    val requireRelativeHeight: Int = display.heightPixels - binding.topBackground.height
                    val n = r.size / 6
                    val num = if (r.size > 0 && r.size % 6 == 0) n else n + 1
                    val actualRelativeHeight = resources.getDimension(R.dimen._224dp).toInt() * num
                    Timber.d("填充不够滚动的空间: $actualRelativeHeight, $requireRelativeHeight")
                    if (actualRelativeHeight < requireRelativeHeight) {
                        container.setPadding(
                            0, 0, 0, requireRelativeHeight - actualRelativeHeight
                        )
                    }
                }
            }
        }
    }

    private fun initialProgressBar() {
        binding.pbMediaProgress.max = MAX_PROGRESS
        binding.topPbMediaProgress.max = MAX_PROGRESS
        binding.topPbMediaProgress2.max = MAX_PROGRESS
        isTouchingSeekBar = false
        viewModel.mediaProgress.observe(viewLifecycleOwner) { progress ->
            if (!isTouchingSeekBar) {
                binding.pbMediaProgress.setProgress(progress, false)
                binding.pbMediaProgress.secondaryProgress = viewModel.bufferProgress
                binding.topPbMediaProgress.setProgress(progress, false)
                binding.topPbMediaProgress.secondaryProgress = viewModel.bufferProgress
                binding.topPbMediaProgress2.setProgress(progress, false)
                binding.topPbMediaProgress2.secondaryProgress = viewModel.bufferProgress
            }
        }

        binding.pbMediaProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTouchingSeekBar = true

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //                isTouchingSeekBar = false
                if (binding.pbMediaProgress.isVisible) {
                    seekToPosition(seekBar) {
                        isTouchingSeekBar = false
                    }
                }
            }
        })

        binding.topPbMediaProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTouchingSeekBar = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                isTouchingSeekBar = false
                if (binding.topPbMediaProgress.isVisible) {
                    seekToPosition(seekBar) {
                        isTouchingSeekBar = false
                    }
                }
            }
        })

        binding.topPbMediaProgress2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTouchingSeekBar = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                isTouchingSeekBar = false
                if (binding.topPbMediaProgress2.isVisible) {
                   seekToPosition(seekBar) {
                       isTouchingSeekBar = false
                   }
                }
            }
        })
    }

    private fun seekToPosition(seekBar: SeekBar?, complete: () -> Unit) {
        viewModel.mediaMetadata.value?.let {
            val posMs = (it.duration * (seekBar?.progress ?: 0f).toFloat() / MAX_PROGRESS.toFloat()).roundToLong()
            viewModel.seekToPosition(
                if (posMs < 0) 0 else if (posMs > it.duration) it.duration else posMs
            ) {
                CoroutineScope(Dispatchers.Default).launch {
                    delay(100)
                    withContext(Dispatchers.Main) {
                        complete()
                    }
                }
            }
        }
    }

    /**
     * 滚动位置事件处理
     */
    private fun handleTransform(scrollY: Int) {
        currentScrollY = scrollY
        // 当前播放向上滚动的百分比，由100%逐渐减小到0
        if (scrollY > SCROLL_THRESHOLD) {
            var percent: Float = 1f - scrollY.toFloat() / coverImgY
            if (percent > 1f) percent = 1f
            if (percent < 0f) percent = 0f

            transformCoverImage(percent)
            transformAlbumTitle(percent)
            transformProgressBar(percent)

            binding.tvMediaTitle.alpha = percent
            binding.tvMediaSubtitle.alpha = percent
            binding.tvMediaDuration.alpha = percent
            binding.tvMeidaPlayPosition.alpha = percent
            binding.tvMediaSubscribe.alpha = percent
            binding.btnSubscribe.alpha = percent

            // 加载背景
            if (percent > 0.4f) {
                binding.topBackground.visibility = View.VISIBLE
                binding.topBackground.alpha = 1f - (percent - 0.4f) / (1f - 0.4f)
            } else {
                binding.topBackground.alpha = 1f
            }

            // 向上滚动封面，到40%时开始变换显示内容
            if (percent <= 0.4f) {
                val alpha = (0.4f - percent) / 0.4f
                // 显示top view
                binding.topTvMediaTitle.visibility = View.VISIBLE
                binding.topTvMediaSubtitle.visibility = View.VISIBLE
                binding.topBtnSubscribe.visibility = View.VISIBLE

                binding.topTvMediaTitle.alpha = alpha
                binding.topTvMediaSubtitle.alpha = alpha
                binding.topBtnSubscribe.alpha = alpha

                // 逐渐显示二级背景
                binding.topIvBackground.visibility = View.VISIBLE
                binding.topIvBackground.alpha = alpha
            }  else {
                binding.topTvMediaTitle.visibility = View.INVISIBLE
                binding.topTvMediaSubtitle.visibility = View.INVISIBLE
                binding.topBtnSubscribe.visibility = View.INVISIBLE

                binding.topIvBackground.visibility = View.INVISIBLE
            }

            // 向上滚动到底层进度条
            if (scrollY > progressBarScrollDiff) {
                var pbPercent: Float = 1f - (scrollY.toFloat() - progressBarScrollDiff) / (relativeAlbumExtra1ScrollDiff - progressBarScrollDiff)
                if (pbPercent > 1f) pbPercent = 1f
                if (pbPercent < 0f) pbPercent = 0f
                binding.topBtnMediaPlay.visibility = View.VISIBLE
                binding.topBtnMediaPlay.translationY = binding.topBtnMediaPlay.height.toFloat() / 3 * pbPercent
                binding.topBtnMediaPlay.alpha = 1 - pbPercent

                binding.topBtnMediaNext15s.visibility = View.VISIBLE
                binding.topBtnMediaNext15s.translationY = binding.topBtnMediaNext15s.height.toFloat() / 3 * pbPercent
                binding.topBtnMediaNext15s.alpha = 1 - pbPercent

                binding.topBtnMediaMultiple.visibility = View.VISIBLE
                binding.topBtnMediaMultiple.translationY = binding.topBtnMediaMultiple.height.toFloat() / 3 * pbPercent
                binding.topBtnMediaMultiple.alpha = 1 - pbPercent
            } else {
                binding.topBtnMediaPlay.visibility = View.INVISIBLE
                binding.topBtnMediaNext15s.visibility = View.INVISIBLE
                binding.topBtnMediaMultiple.visibility = View.INVISIBLE
            }

            // 向上滚到到推荐内容
            if (scrollY >= relativeAlbumExtra1ScrollDiff) {
                binding.topTvMediaRelativeExtra1.visibility = View.VISIBLE
                binding.topTvMediaRelativeExtra2.visibility = View.VISIBLE
                binding.topTvMediaRelativeMore.visibility = View.VISIBLE
                binding.topBackground2.visibility = View.VISIBLE

                binding.layoutRelativeAlbums.tvMediaRelativeExtra1.visibility = View.INVISIBLE
                binding.layoutRelativeAlbums.tvMediaRelativeExtra2.visibility = View.INVISIBLE
                binding.layoutRelativeAlbums.tvMediaRelativeMore.visibility = View.INVISIBLE

                // 显示第三状态进度条
                binding.topPbMediaProgress2.visibility = View.VISIBLE
                binding.topPbMediaProgress.visibility = View.INVISIBLE

                binding.topScrollCover.root.visibility = if (scrollY > relativeAlbumExtra1ScrollDiff) View.VISIBLE else View.INVISIBLE
            } else {
                // 向下滚出推荐内容
                binding.topTvMediaRelativeExtra1.visibility = View.INVISIBLE
                binding.topTvMediaRelativeExtra2.visibility = View.INVISIBLE
                binding.topTvMediaRelativeMore.visibility = View.INVISIBLE
                binding.topBackground2.visibility = View.INVISIBLE

                binding.layoutRelativeAlbums.tvMediaRelativeExtra1.visibility = View.VISIBLE
                binding.layoutRelativeAlbums.tvMediaRelativeExtra2.visibility = View.VISIBLE
                binding.layoutRelativeAlbums.tvMediaRelativeMore.visibility = View.VISIBLE

                // 隐藏第三状态进度条
                binding.topPbMediaProgress2.visibility = View.INVISIBLE
                binding.topPbMediaProgress.visibility = View.VISIBLE

                binding.topScrollCover.root.visibility = View.INVISIBLE
            }
        } else {
            binding.ivMediaCoverImg.visibility = View.VISIBLE
            binding.tvMediaAlbumTitle.visibility = View.VISIBLE
            binding.pbMediaProgress.visibility = View.VISIBLE

            // 隐藏top view
            binding.topIvMediaCoverImg.visibility = View.INVISIBLE
            binding.topTvMediaAlbumTitle.visibility = View.INVISIBLE

            binding.topTvMediaTitle.visibility = View.INVISIBLE
            binding.topTvMediaSubtitle.visibility = View.INVISIBLE
            binding.topBackground.visibility = View.INVISIBLE
            binding.topPbMediaProgress.visibility = View.INVISIBLE

        }
    }

    private fun transformCoverImage(percent: Float) {
        val coverImg = binding.ivMediaCoverImg
        val topCoverImg = binding.topIvMediaCoverImg
        coverImg.visibility = View.INVISIBLE
        topCoverImg.visibility = View.VISIBLE
        // 设置动画中心
        topCoverImg.pivotX = 0f
        topCoverImg.pivotY = 0f
        topCoverImg.scaleX = 1f + (coverImg.width / topCoverImg.width.toFloat() - 1f) * percent
        topCoverImg.scaleY = 1f + (coverImg.height / topCoverImg.height.toFloat() - 1f) * percent
        val yDiff = coverImgY - topCoverImgY
        topCoverImg.translationY = (if (yDiff > 0f) yDiff else 0f) * percent
        val xDiff = topCoverImgX - coverImgX
        topCoverImg.translationX = -(if (xDiff > 0f) xDiff else 0f) * percent

    }

    private fun transformAlbumTitle(percent: Float) {
        val tvAlbumTitle = binding.tvMediaAlbumTitle
        val topTvAlbumTitle = binding.topTvMediaAlbumTitle
        topTvAlbumTitle.pivotX = 0f
        topTvAlbumTitle.pivotY = 0f
        tvAlbumTitle.visibility = View.INVISIBLE
        topTvAlbumTitle.visibility = View.VISIBLE

        val yDiff = tvAlbumTitleY - topTvAlbumTitleY
        topTvAlbumTitle.translationY = (if (yDiff > 0f) yDiff else 0f) * percent
        val xDiff = topTvAlbumTitleX - tvAlbumTitleX
        topTvAlbumTitle.translationX = -(if (xDiff > 0f) xDiff else 0f) * percent
    }

    private fun transformProgressBar(percent: Float) {
        val pbMediaProgress = binding.pbMediaProgress
        val topPbMediaProgress = binding.topPbMediaProgress
        pbMediaProgress.visibility = View.INVISIBLE
        topPbMediaProgress.visibility = View.VISIBLE
        topPbMediaProgress.scaleX = 1f + (pbMediaProgress.width / topPbMediaProgress.width.toFloat() - 1f) * percent

        val yDiff = pbMediaProgressY - topPbMediaProgressY
        topPbMediaProgress.translationY = (if (yDiff > 0f) yDiff else 0f) * percent
    }

    /**
     * 倍数播放
     */
    private fun showMultiplePlayDialog() {
        selectedMultipleIndex = viewModel.repo.prefs.selectedMultipleIndex
        PromptDialog.Builder(requireContext())
            .setViewId(R.layout.dialog_multiple_play)
            .isTransparent(true)
            .configView { v, requestDismiss ->
                val container = v.findViewById<FlexboxLayout>(R.id.container_dialog_select_items)
                val items = arrayOf(
                    "0.5x", "0.75x", "1.0x", "1.25x", "1.5x", "2.0x"
                )
                items.forEachIndexed { index, item ->
                    val tv = TextView(context)
                    container.addView(tv)
                    tv.tag = index
                    val lp = tv.layoutParams as FlexboxLayout.LayoutParams
                    lp.width = resources.getDimension(R.dimen._162dp).toInt()
                    lp.height = resources.getDimension(R.dimen._52dp).toInt()
                    lp.topMargin = resources.getDimension(R.dimen._48dp).toInt()

                    tv.text = item
                    tv.setBackgroundResource(R.drawable.shape_album_tag)
                    tv.gravity = Gravity.CENTER
                    tv.setTextColor(resources.getColor(R.color.color_42444B))
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._22dp))

                    selectPlayTag(tv, selectedMultipleIndex)

                    tv.setOnClickListener { v ->
                        val vIndex = v.tag as Int
                        selectedMultipleIndex = vIndex
                        viewModel.repo.prefs.selectedMultipleIndex = selectedMultipleIndex
                        viewModel.setPlaySpeed(
                            when(vIndex) {
                                0 -> 0.5f
                                1 -> 0.75f
                                2 -> 1f
                                3 -> 1.25f
                                4 -> 1.5f
                                5 -> 2.0f
                                else -> 1f
                            }
                        )
                        requestDismiss.invoke()
                        container.children.forEach {
                            selectPlayTag(it as TextView, selectedMultipleIndex)
                        }
                    }
                }
            }
            .addOperation(OperationType.CANCEL, R.id.btn_dialog_cancel, true, null)
            .build()
            .show()
    }

    /**
     * 定时播放
     */
    private fun showTimingPlay() {
        selectedTimingIndex = viewModel.repo.prefs.selectedTimingIndex
        PromptDialog.Builder(requireContext())
            .setViewId(R.layout.dialog_multiple_play)
            .isTransparent(true)
            .configView { v, requestDismiss ->
                val container = v.findViewById<FlexboxLayout>(R.id.container_dialog_select_items)
                container.justifyContent = JustifyContent.FLEX_START
                val items = arrayOf(
                    "不开启", "播完本集", "播完下一级", "10分钟后", "20分钟后", "30分钟后", "60分钟后", "90分钟后"
                )
                items.forEachIndexed { index, item ->
                    val tv = TextView(context)
                    container.addView(tv)
                    tv.tag = index
                    val lp = tv.layoutParams as FlexboxLayout.LayoutParams
                    lp.width = resources.getDimension(R.dimen._162dp).toInt()
                    lp.height = resources.getDimension(R.dimen._52dp).toInt()
                    lp.topMargin = resources.getDimension(R.dimen._48dp).toInt()
                    if (index % 3 != 0) {
                        lp.leftMargin = resources.getDimension(R.dimen._24dp).toInt()
                    }
                    tv.text = item
                    tv.setBackgroundResource(R.drawable.shape_album_tag)
                    tv.gravity = Gravity.CENTER
                    tv.setTextColor(resources.getColor(R.color.color_42444B))
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._22dp))

                    selectPlayTag(tv, selectedTimingIndex)

                    tv.setOnClickListener { v ->
                        val vIndex = v.tag as Int
                        selectedTimingIndex = vIndex
                        viewModel.repo.prefs.selectedTimingIndex = selectedTimingIndex
                        when (vIndex) {
                            0 -> viewModel.resetTimingConfig()
                            1 -> viewModel.stopOnThisEnd()
                            2 -> viewModel.stopOnNextEnd()
                            3 -> viewModel.stopAfterTime(10)
                            4 -> viewModel.stopAfterTime(20)
                            5 -> viewModel.stopAfterTime(30)
                            6 -> viewModel.stopAfterTime(60)
                            7 -> viewModel.stopAfterTime(90)
                        }
                        requestDismiss.invoke()
                        container.children.forEach {
                            selectPlayTag(it as TextView, selectedTimingIndex)
                        }
                    }
                }
            }
            .addOperation(OperationType.CANCEL, R.id.btn_dialog_cancel, true, null)
            .build()
            .show()
    }

    private fun selectPlayTag(view: TextView, selectedIndex: Int) {
        val vIndex = view.tag as Int
        if (vIndex == selectedIndex) {
            // 选中
            view.setBackgroundResource(R.drawable.shape_album_tag_selected)
            view.setTextColor(Color.WHITE)
        } else {
            // 未选中
            view.setBackgroundResource(R.drawable.shape_album_tag)
            view.setTextColor(resources.getColor(R.color.color_42444B))
        }
    }

    private fun subscribe() {
        val id = viewModel.repo.prefs.nowPlayingAlbumId
        if (id?.isNotEmpty() == true) {
            viewModel.subscribeAlbum(id.toInt()).observe(viewLifecycleOwner) {
                handleResponse(it) { r ->
                    if (r.code == 200) {
                        ToastUtil.show(context, "订阅成功")
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_ALBUM_TITLE = "ARG_ALBUM_TITLE"
        private const val SCROLL_THRESHOLD = 0
        const val MAX_PROGRESS = 1000

        fun newInstance(albumTitle: String) = NowPlayingFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ALBUM_TITLE, albumTitle)
            }
        }
    }
}