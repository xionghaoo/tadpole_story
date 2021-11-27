package xh.zero.tadpolestory.ui.album

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentTrackListBinding
import javax.inject.Inject

@AndroidEntryPoint
class TrackListFragment : Fragment() {

    @Inject
    lateinit var albumViewModelFactory: AlbumViewModel.AssistedFactory
    private val viewModel: AlbumViewModel by viewModels {
        AlbumViewModel.provideFactory(albumViewModelFactory, albumId)
    }

    private val albumId: String by lazy {
        arguments?.getString(ARG_ALBUM_ID) ?: ""
    }

    private val total: Int by lazy {
        arguments?.getInt(ARG_TOTAL, 0) ?: 0
    }

    private lateinit var binding: FragmentTrackListBinding
    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTotalAlbum.text = "共${total}集"
        binding.rcTrackList.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = TrackAdapter(emptyList()) { item ->
            viewModel.playMedia(item, pauseAllowed = false)
            // 显示正在播放页面
        }
        binding.rcTrackList.adapter = adapter
        loadData()
    }

    private fun loadData() {
        viewModel.mediaItems.observe(viewLifecycleOwner) { items ->
            Timber.d("加载的音频数量：${items.size}")
            if (items.isNotEmpty()) {
                adapter.updateData(items)
            }
        }
    }

    companion object {

        private const val ARG_ALBUM_ID = "ARG_ALBUM_ID"
        private const val ARG_TOTAL = "ARG_TOTAL"

        fun newInstance(albumId: String, total: Int) = TrackListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ALBUM_ID, albumId)
                putInt(ARG_TOTAL, total)
            }
        }
    }
}