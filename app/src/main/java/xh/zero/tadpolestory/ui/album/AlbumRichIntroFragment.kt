package xh.zero.tadpolestory.ui.album

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentAlbumRichIntroBinding
import xh.zero.tadpolestory.ui.BaseFragment

class AlbumRichIntroFragment : BaseFragment<FragmentAlbumRichIntroBinding>() {

    private val args: AlbumRichIntroFragmentArgs by navArgs()

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAlbumRichIntroBinding {
        return FragmentAlbumRichIntroBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.tvRichInfo.setText(Html.fromHtml(args.albumRichInfo))
    }
}