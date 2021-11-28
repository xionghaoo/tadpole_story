package xh.zero.tadpolestory.ui.album

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.drawable.updateBounds
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxItemDecoration
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.GlideApp
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentNowPlayingBinding
import xh.zero.tadpolestory.handleResponse
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@AndroidEntryPoint
class NowPlayingFragment : Fragment() {

    private lateinit var binding: FragmentNowPlayingBinding
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

    private lateinit var relativeAlbumAdapter: RelativeAlbumAdapter
    private val albumTitle: String by lazy {
        arguments?.getString(ARG_ALBUM_TITLE) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mediaMetadata.observe(viewLifecycleOwner) { mediaItem ->
            updateUI(mediaItem)
        }

        viewModel.mediaPosition.observe(viewLifecycleOwner) { pos ->
            binding.tvMeidaPlayPosition.text = NowPlayingViewModel.NowPlayingMetadata.timestampToMSS(pos)
        }
        viewModel.mediaButtonRes.observe(viewLifecycleOwner) { res ->
            binding.btnMediaPlay.setImageResource(res)
        }

        initialProgressBar()

        binding.btnMediaPlay.setOnClickListener {
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
                Timber.d("current scroll y = $currentScrollY, $progressBarScrollDiff, $relativeAlbumExtra1ScrollDiff")
                CoroutineScope(Dispatchers.Default).launch {
                    delay(100)
                    withContext(Dispatchers.Main) {
                        if (currentScrollY < progressBarScrollDiff / 3) {
                            binding.scrollView.smoothScrollTo(0, 0)
                        } else if (currentScrollY >= progressBarScrollDiff / 3 && currentScrollY < progressBarScrollDiff) {
                            binding.scrollView.smoothScrollTo(0, progressBarScrollDiff.toInt())
                        } else if (currentScrollY < progressBarScrollDiff + (relativeAlbumExtra1ScrollDiff - progressBarScrollDiff) / 3) {
                            binding.scrollView.smoothScrollTo(0, progressBarScrollDiff.toInt())
                        } else if (currentScrollY < relativeAlbumExtra1ScrollDiff){
                            binding.scrollView.smoothScrollTo(0, relativeAlbumExtra1ScrollDiff.toInt())
                        }
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

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
    }

    private fun loadRelativeAlbum(trackId: Int) {
//        binding.layoutRelativeAlbums.rcRelativeAlbums.layoutManager = GridLayoutManager(requireContext(), 6)
//        relativeAlbumAdapter = RelativeAlbumAdapter(emptyList()) {
//
//        }
//        binding.layoutRelativeAlbums.rcRelativeAlbums.adapter = relativeAlbumAdapter
        viewModel.getRelativeAlbum(trackId).observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
//                relativeAlbumAdapter.updateData(r)
                val container = binding.layoutRelativeAlbums.rcRelativeAlbums
                val itemDecoration = FlexboxItemDecoration(requireContext())
                itemDecoration.setOrientation(FlexboxItemDecoration.HORIZONTAL)
                container.removeAllViews()
                r.forEachIndexed { index, item ->
                    val v = LayoutInflater.from(requireContext()).inflate(R.layout.list_item_relative_album, null)
                    v.findViewById<TextView>(R.id.tv_album_title).text = item.album_title
                    v.findViewById<TextView>(R.id.tv_album_desc).text = item.album_intro
                    Glide.with(v.context)
                        .load(item.cover_url_large)
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
                }
            }
        }
    }

    private fun initialProgressBar() {
        isTouchingSeekBar = false
        viewModel.mediaProgress.observe(viewLifecycleOwner) { progress ->
            if (!isTouchingSeekBar) {
                binding.pbMediaProgress.setProgress(progress, true)
                binding.topPbMediaProgress.setProgress(progress, true)
                binding.topPbMediaProgress2.setProgress(progress, true)
            }
        }

        binding.pbMediaProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTouchingSeekBar = true

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTouchingSeekBar = false
                if (binding.pbMediaProgress.isVisible) {
                    seekToPosition(seekBar)
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
                isTouchingSeekBar = false
                if (binding.topPbMediaProgress.isVisible) {
                    seekToPosition(seekBar)
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
                isTouchingSeekBar = false
                if (binding.topPbMediaProgress2.isVisible) {
                   seekToPosition(seekBar)
                }
            }
        })
    }

    private fun seekToPosition(seekBar: SeekBar?) {
        viewModel.mediaMetadata.value?.let {
            val posMs = (it.duration * (seekBar?.progress ?: 0f).toFloat() / 500f).roundToLong()
            viewModel.seekToPosition(
                if (posMs < 0) 0 else if (posMs > it.duration) it.duration else posMs
            ) {
                viewModel.rePlay()
            }
        }
    }

    private fun handleTransform(scrollY: Int) {
        currentScrollY = scrollY
        // 当前播放向上滚动的百分比，由100%逐渐减小到0
        if (scrollY > SCROLL_THRESHOLD) {
            var percent: Float = 1f - (scrollY).toFloat() / coverImgY
            if (percent > 1f) percent = 1f
            if (percent < 0f) percent = 0f
            Timber.d("handleTransform:: percent: $percent, ${relativeAlbumExtra1ScrollDiff}, $scrollY")

            transformCoverImage(percent)
            transformAlbumTitle(percent)
            transformProgressBar(percent)

            binding.tvMediaTitle.alpha = percent
            binding.tvMediaSubtitle.alpha = percent
            binding.tvMediaDuration.alpha = percent
            binding.tvMeidaPlayPosition.alpha = percent
            binding.tvMediaSubscribe.alpha = percent
            binding.btnSubscribe.alpha = percent

//            binding.topTvMediaAlbumTitle.visibility = View.VISIBLE

            // 加载背景
            if (percent > 0.4f) {
                binding.topBackground.visibility = View.VISIBLE
                binding.topBackground.alpha = 1f - (percent - 0.4f) / (1f - 0.4f)
            } else {
                binding.topBackground.alpha = 1f
            }

            if (percent <= 0.4f) {
                val alpha = (0.4f - percent) / 0.4f
                // 显示top view
                binding.topTvMediaTitle.visibility = View.VISIBLE
                binding.topTvMediaSubtitle.visibility = View.VISIBLE
                binding.topTvMediaTitle.alpha = alpha
                binding.topTvMediaSubtitle.alpha = alpha
            }  else {
//                binding.topTvMediaAlbumTitle.visibility = View.INVISIBLE
                binding.topTvMediaTitle.visibility = View.INVISIBLE
                binding.topTvMediaSubtitle.visibility = View.INVISIBLE
            }

            // 滚动到进度条时
//            if (scrollY > progressBarScrollDiff) {
//                binding.topPbMediaProgress.visibility = View.VISIBLE
//                binding.pbMediaProgress.visibility = View.INVISIBLE
//            } else {
//                binding.topPbMediaProgress.visibility = View.INVISIBLE
//                binding.pbMediaProgress.visibility = View.VISIBLE
//            }

            // 滚到到推荐内容
            if (scrollY > relativeAlbumExtra1ScrollDiff) {
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
            } else {
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
//        topTvAlbumTitle.scaleX = 1f + (tvAlbumTitle.width / topTvAlbumTitle.width.toFloat() - 1f) * percent
//        topTvAlbumTitle.scaleY = 1f + (tvAlbumTitle.height / topTvAlbumTitle.height.toFloat() - 1f) * percent

        val yDiff = tvAlbumTitleY - topTvAlbumTitleY
        topTvAlbumTitle.translationY = (if (yDiff > 0f) yDiff else 0f) * percent
        val xDiff = topTvAlbumTitleX - tvAlbumTitleX
        topTvAlbumTitle.translationX = -(if (xDiff > 0f) xDiff else 0f) * percent
    }

    private fun transformProgressBar(percent: Float) {
        val pbMediaProgress = binding.pbMediaProgress
        val topPbMediaProgress = binding.topPbMediaProgress
//        topPbMediaProgress.pivotX = 0f
        pbMediaProgress.visibility = View.INVISIBLE
        topPbMediaProgress.visibility = View.VISIBLE
        topPbMediaProgress.scaleX = 1f + (pbMediaProgress.width / topPbMediaProgress.width.toFloat() - 1f) * percent

        val yDiff = pbMediaProgressY - topPbMediaProgressY
        topPbMediaProgress.translationY = (if (yDiff > 0f) yDiff else 0f) * percent
    }

    private fun transformView(
        animView: View,
        originView: View,
        percent: Float,
//        hasTransformX: Boolean,
    ) {
//        val coverImg = binding.ivMediaCoverImg
//        val topCoverImg = binding.topIvMediaCoverImg
        originView.visibility = View.INVISIBLE
        animView.visibility = View.VISIBLE
        // 设置动画中心
        animView.pivotX = 0f
        animView.pivotY = 0f
        animView.scaleX = 1f + (originView.width / animView.width.toFloat() - 1f) * percent
        animView.scaleY = 1f + (originView.height / animView.height.toFloat() - 1f) * percent
        val yDiff = coverImgY - topCoverImgY
        animView.translationY = (if (yDiff > 0f) yDiff else 0f) * percent
        val xDiff = topCoverImgX - coverImgX
        animView.translationX = -(if (xDiff > 0f) xDiff else 0f) * percent
    }

    companion object {
        private const val ARG_ALBUM_TITLE = "ARG_ALBUM_TITLE"
        private const val SCROLL_THRESHOLD = 0

        fun newInstance(albumTitle: String) = NowPlayingFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ALBUM_TITLE, albumTitle)
            }
        }
    }
}