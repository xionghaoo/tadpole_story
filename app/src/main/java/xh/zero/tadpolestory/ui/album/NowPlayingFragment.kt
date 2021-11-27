package xh.zero.tadpolestory.ui.album

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.GlideApp
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentNowPlayingBinding
import kotlin.math.roundToLong

@AndroidEntryPoint
class NowPlayingFragment : Fragment() {

    private lateinit var binding: FragmentNowPlayingBinding
    private val viewModel: NowPlayingViewModel by viewModels()
    private var isTouchingSeekBar = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.mediaMetadata.observe(viewLifecycleOwner) { mediaItem ->
            binding.tvMediaDuration.text = NowPlayingViewModel.NowPlayingMetadata.timestampToMSS(mediaItem.duration)
            binding.tvMediaTitle.text = mediaItem.title
            binding.tvMediaSubtitle.text = mediaItem.subtitle
            GlideApp.with(this)
                .load(mediaItem.albumArtUri)
                .into(binding.ivMediaCoverImg)
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


    }

    companion object {
        fun newInstance() = NowPlayingFragment()
    }
}