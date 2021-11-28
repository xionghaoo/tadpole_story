package xh.zero.tadpolestory.ui.album

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.tadpolestory.GlideApp
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentNowPlayingBinding
import xh.zero.tadpolestory.handleResponse
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@AndroidEntryPoint
class NowPlayingFragment : Fragment() {

    private lateinit var binding: FragmentNowPlayingBinding
    private val viewModel: NowPlayingViewModel by viewModels()
    private var isTouchingSeekBar = false

    private var hasInit = false
    private var originX = 0f
    private var originY = 0f
    private var targetX = 0f
    private var targetY = 0f

    private lateinit var relativeAlbumAdapter: RelativeAlbumAdapter

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
            binding.tvMediaDuration.text = NowPlayingViewModel.NowPlayingMetadata.timestampToMSS(mediaItem.duration)
            binding.tvMediaTitle.text = mediaItem.title
            binding.topTvMediaTitle.text = mediaItem.title
            binding.tvMediaSubtitle.text = mediaItem.subtitle
            binding.topTvMediaSubtitle.text = mediaItem.subtitle
            GlideApp.with(this)
                .load(mediaItem.albumArtUri)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(resources.getDimension(R.dimen._24dp).roundToInt())))
                .into(binding.ivMediaCoverImg)


            val rate = resources.getDimension(R.dimen._140dp) / resources.getDimension(R.dimen._250dp)
            GlideApp.with(this)
                .load(mediaItem.albumArtUri)
                .apply(RequestOptions.bitmapTransform(RoundedCorners((resources.getDimension(R.dimen._24dp) * rate).roundToInt())))
                .into(binding.topIvMediaCoverImg)

            loadRelativeAlbum(mediaItem.id.toInt())

        }

        viewModel.mediaPosition.observe(viewLifecycleOwner) { pos ->
            binding.tvMeidaPlayPosition.text = NowPlayingViewModel.NowPlayingMetadata.timestampToMSS(pos)
        }
        viewModel.mediaButtonRes.observe(viewLifecycleOwner) { res ->
            binding.btnMediaPlay.setImageResource(res)
        }

        isTouchingSeekBar = false
        viewModel.mediaProgress.observe(viewLifecycleOwner) { progress ->
            if (!isTouchingSeekBar) {
                binding.pbMediaProgress.setProgress(progress, true)
            }
        }

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

        binding.pbMediaProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTouchingSeekBar = true

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTouchingSeekBar = false
                viewModel.mediaMetadata.value?.let {
                    val posMs = (it.duration * (seekBar?.progress ?: 0f).toFloat() / 500f).roundToLong()
                    viewModel.seekToPosition(
                        if (posMs < 0) 0 else if (posMs > it.duration) it.duration else posMs
                    ) {
                        viewModel.rePlay()
                    }
                }
            }
        })


        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            if (!hasInit) {
                hasInit = true
                val target = binding.ivMediaCoverImg
                val origin = binding.topIvMediaCoverImg
                targetX = target.x
                targetY = binding.containerPlayer.y
                originX = origin.x
                originY = origin.y
            }
        }

        binding.scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > 0) {
                // 起始状态 0 -> topIvMediaCoverImg.y
                val target = binding.ivMediaCoverImg
                val origin = binding.topIvMediaCoverImg
                target.visibility = View.INVISIBLE
                var percent: Float = 1f - (scrollY).toFloat() / targetY
                if (percent > 1f) percent = 1f
                if (percent < 0f) percent = 0f
                origin.pivotX = 0f
                origin.pivotY = 0f
                origin.visibility = View.VISIBLE
                origin.scaleX = 1f + (target.width / origin.width.toFloat() - 1f) * percent
                origin.scaleY = 1f + (target.height / origin.height.toFloat() - 1f) * percent
                val yDiff = targetY - originY
                origin.translationY = (if (yDiff > 0f) yDiff else 0f) * percent
                val xDiff = originX - targetX
                origin.translationX = -(if (xDiff > 0f) xDiff else 0f) * percent

                binding.tvMediaTitle.alpha = percent
                binding.tvMediaSubtitle.alpha = percent
                binding.tvMediaDuration.alpha = percent
                binding.tvMeidaPlayPosition.alpha = percent
                binding.tvMediaSubscribe.alpha = percent
                binding.btnSubscribe.alpha = percent

                binding.topTvMediaTitle.visibility = View.VISIBLE
                binding.topTvMediaSubtitle.visibility = View.VISIBLE
                binding.topTvMediaAlbumTitle.visibility = View.VISIBLE
                binding.topTvMediaTitle.alpha = 1f - percent
                binding.topTvMediaSubtitle.alpha = 1f - percent


            } else {
                binding.topIvMediaCoverImg.visibility = View.INVISIBLE
                binding.ivMediaCoverImg.visibility = View.VISIBLE

                binding.topTvMediaTitle.visibility = View.INVISIBLE
                binding.topTvMediaSubtitle.visibility = View.INVISIBLE
                binding.topTvMediaAlbumTitle.visibility = View.INVISIBLE
            }
        }

    }

    private fun loadRelativeAlbum(trackId: Int) {
        binding.layoutRelativeAlbums.rcRelativeAlbums.layoutManager = GridLayoutManager(requireContext(), 6)
        relativeAlbumAdapter = RelativeAlbumAdapter(emptyList()) {

        }
        binding.layoutRelativeAlbums.rcRelativeAlbums.adapter = relativeAlbumAdapter

        viewModel.getRelativeAlbum(trackId).observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
                relativeAlbumAdapter.updateData(r)
            }
        }
    }

    companion object {
        fun newInstance() = NowPlayingFragment()
    }
}