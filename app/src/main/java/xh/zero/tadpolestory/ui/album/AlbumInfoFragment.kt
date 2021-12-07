package xh.zero.tadpolestory.ui.album

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentAlbumInfoBinding
import xh.zero.tadpolestory.ui.BaseFragment

class AlbumInfoFragment : BaseFragment<FragmentAlbumInfoBinding>() {

    private val albumIntro: String by lazy {
        arguments?.getString(ARG_ALBUM_INTRO) ?: ""
    }

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAlbumInfoBinding {
        return FragmentAlbumInfoBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvAlbumIntro.text = albumIntro
        binding.btnAlbumIntroAll.setOnClickListener {
            ToastUtil.show(context, "查看更多")
        }
    }

    companion object {
        private const val ARG_ALBUM_INTRO = "ARG_ALBUM_INTRO"

        fun newInstance(intro: String) = AlbumInfoFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ALBUM_INTRO, intro)
            }
        }
    }
}