package xh.zero.tadpolestory.ui.album

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import xh.zero.core.replaceFragment
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentAlbumDetailBinding
import xh.zero.tadpolestory.replaceFragment
import xh.zero.tadpolestory.ui.MainFragmentDirections

class AlbumDetailFragment : Fragment() {

    private lateinit var binding: FragmentAlbumDetailBinding

    val args: AlbumDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        replaceFragment(TrackListFragment.newInstance(args.albumId.toString(), args.totalCount, args.albumTitle), R.id.fragment_container)
    }

//    companion object {
//
//        const val ARG_ALBUM_ID = "${Configs.PACKAGE_NAME}.AlbumDetailActivity.ARG_ALBUM_ID"
//        const val ARG_TOTAL = "${Configs.PACKAGE_NAME}.AlbumDetailActivity.ARG_TOTAL"
//        const val ARG_ALBUM_TITLE = "${Configs.PACKAGE_NAME}.AlbumDetailActivity.ARG_ALBUM_TITLE"
//
//        fun newInstance(albumId: Int, total: Int, albumTitle: String) =
//            AlbumDetailFragment().apply {
//                arguments = Bundle().apply {
//                    putInt(ARG_ALBUM_ID, albumId)
//                    putInt(ARG_TOTAL, total)
//                    putString(ARG_ALBUM_TITLE, albumTitle)
//                }
//            }
//    }
}