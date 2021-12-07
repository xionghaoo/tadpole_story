package xh.zero.tadpolestory.ui.album

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.startPlainActivity
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

    private val total: Long by lazy {
        arguments?.getLong(ARG_TOTAL, 0) ?: 0
    }

    private val albumTitle: String by lazy {
        arguments?.getString(ARG_ALBUM_TITLE) ?: ""
    }

    private var totalScrollY = 0
    private var isShowTrackSelectPanel = false
    private var selectedIndex = 0

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
        viewModel.repo.prefs.nowPlayingAlbumId = albumId

        binding.tvTotalAlbum.text = "共${total}集"
        binding.rcTrackList.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = TrackAdapter(emptyList()) { item ->
            viewModel.playMedia(item, pauseAllowed = false)
            // 显示正在播放页面
            NowPlayingActivity.start(context, albumTitle)
        }
        binding.rcTrackList.adapter = adapter
        binding.rcTrackList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                totalScrollY += dy
                binding.vScrollCover.visibility = if (totalScrollY > 0) View.VISIBLE else View.INVISIBLE
            }
        })
        binding.rcTrackList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                if (binding.rcTrackList.layoutManager.la())
                // 如果最后一项可见，加载更多
            }
        })

        binding.btnAlbumTracks.setOnClickListener {
            toggleTrackSelectPanel()
        }

        loadData()

    }

    private fun loadData() {
        viewModel.loadSongs(1, false)
        viewModel.mediaItems.observe(viewLifecycleOwner) { items ->
            Timber.d("加载的音频数量：${items.size}")
            if (items.isNotEmpty()) {
                adapter.updateData(items)

                createTrackSelectView(listOf("1-20", "21-30"))
            }
        }
    }

    private fun createTrackSelectView(scopes: List<String>) {
        binding.flAlbumTrackList.removeAllViews()
        scopes.forEachIndexed { index, scope ->
            val tv = TextView(context)
            tv.text = scope
            tv.tag = index
            binding.flAlbumTrackList.addView(tv)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._20dp))
            tv.setTextColor(resources.getColor(R.color.color_9B9B9B))
            tv.setPadding(
                resources.getDimension(R.dimen._15dp).toInt(),
                0,
                resources.getDimension(R.dimen._15dp).toInt(),
                0
            )
            tv.gravity = Gravity.CENTER
            tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_album_detail_tag)
            val lp = tv.layoutParams as FlexboxLayout.LayoutParams
            lp.width = FlexboxLayout.LayoutParams.WRAP_CONTENT
            lp.height = resources.getDimension(R.dimen._42dp).toInt()
            lp.rightMargin = resources.getDimension(R.dimen._16dp).toInt()
            lp.minWidth = resources.getDimension(R.dimen._118dp).toInt()

            tv.setOnClickListener { v ->
                selectedIndex = v.tag as Int
                loadTracks()
                binding.flAlbumTrackList.children.forEach { child ->
                    selectTag(child as TextView)
                }
            }
        }
    }

    private fun selectTag(tv: TextView) {
        val viewIndex = tv.tag as Int
        if (viewIndex == selectedIndex) {
            tv.setTextColor(Color.WHITE)
            tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_album_tag_selected)
        } else {
            tv.setTextColor(resources.getColor(R.color.color_9B9B9B))
            tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_album_detail_tag)
        }
    }

    private fun toggleTrackSelectPanel() {
        isShowTrackSelectPanel = isShowTrackSelectPanel.not()
        binding.containerAlbumTrackList.visibility = if (isShowTrackSelectPanel) View.VISIBLE else View.GONE
        binding.ivTrackPanelStatus.setImageResource(
            if (isShowTrackSelectPanel) {
                R.mipmap.ic_up_16
            } else {
                R.mipmap.ic_drop_16
            }
        )
        if (isShowTrackSelectPanel) {
            binding.vTrackSelectPanelCover.visibility = View.VISIBLE
            binding.vTrackSelectPanelCover.setOnClickListener {
                toggleTrackSelectPanel()
            }
            binding.vTrackSelectPanelCover.animate()
                .alpha(1f)
                .start()
        } else {
            binding.vTrackSelectPanelCover.animate()
                .alpha(0f)
                .withEndAction {
                    binding.vTrackSelectPanelCover.visibility = View.GONE
                }
                .start()
        }
    }

    private fun loadTracks() {
        viewModel.loadSongs(2, false)
    }

    companion object {

        private const val ARG_ALBUM_ID = "ARG_ALBUM_ID"
        private const val ARG_TOTAL = "ARG_TOTAL"
        private const val ARG_ALBUM_TITLE = "ARG_ALBUM_TITLE"

        fun newInstance(albumId: String, total: Long, albumTitle: String) = TrackListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ALBUM_ID, albumId)
                putLong(ARG_TOTAL, total)
                putString(ARG_ALBUM_TITLE, albumTitle)
            }
        }
    }
}