package xh.zero.tadpolestory.ui.album

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.vo.NetworkState
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentTrackListBinding
import xh.zero.tadpolestory.ui.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class TrackListFragment : BaseFragment<FragmentTrackListBinding>() {

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
    private var selectedIndex: Int? = null

    private var currentTrackListCoverScrollY = 0

//    private lateinit var binding: FragmentTrackListBinding
    private lateinit var adapter: TrackAdapter
//    private var currentPage = 1
//    private var networkState = MediatorLiveData<NetworkState>()
    private var isInit = true
    private var listener: OnFragmentActionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentActionListener) {
            listener = context
        } else {
            throw IllegalArgumentException("Activity must implement OnFragmentActionListener")
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentTrackListBinding {
        return FragmentTrackListBinding.inflate(layoutInflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onFirstViewCreated")
        isInit = true
        selectedIndex = null

        binding.tvTotalAlbum.text = "共${total}集"
        binding.rcTrackList.layoutManager = LinearLayoutManager(context)
        adapter = TrackAdapter(total.toInt()) { item ->
            viewModel.playMedia(item, pauseAllowed = false)
            // 显示正在播放页面
            NowPlayingActivity.start(context, albumTitle)
        }
        binding.rcTrackList.adapter = adapter
        binding.rcTrackList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                totalScrollY += dy
                binding.vScrollCover.visibility = if (totalScrollY > 0) View.VISIBLE else View.INVISIBLE

                if (!binding.rcTrackList.canScrollVertically(RecyclerView.VERTICAL)
                    && selectedIndex == null
                    && viewModel.networkState.value != NetworkState.LOADING) {
                    if (!isInit) {
                        loadTracksForPage(isRefresh = false, isPaging = true)
                    }
                }

                if (dy > 10) {
                    listener?.hideFloatWindow()
                }
            }
        })
        binding.btnAlbumTracks.setOnClickListener {
            toggleTrackSelectPanel()
        }

        binding.containerAlbumTrackList.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            currentTrackListCoverScrollY = scrollY
            binding.vScrollCoverTrackList.visibility = if (scrollY > 0) View.VISIBLE else View.INVISIBLE
        }

        viewModel.loadMediaItems.observe(this) { items ->
            Timber.d("加载的音频数量：${items.size}")
            // 加载结束
            if (items.isNotEmpty()) {
                adapter.submitList(items)

                if (isInit) {
                    isInit = false
                    binding.rcTrackList.postDelayed({
                        binding.rcTrackList.scrollToPosition(0)
                    }, 100)
                }

                // 音轨分段
                val pageNum = if (total.toInt() % Configs.PAGE_SIZE == 0) total.toInt() / Configs.PAGE_SIZE else total.toInt() / Configs.PAGE_SIZE + 1
                val tags = ArrayList<TrackTag>()
                for (i in 1..pageNum) {
                    val start = 1 + Configs.PAGE_SIZE * (i - 1)
                    var end = i * Configs.PAGE_SIZE
                    if (end > total) end = total.toInt()
                    tags.add(TrackTag(tagName = "$start-$end", page = i))
                }
                createTrackSelectView(tags)
            }
        }

        viewModel.networkState.observe(this) {
            adapter.setNetworkState(it)
        }

        loadTracksForPage(isRefresh = true, isPaging = true)
    }

    private fun loadTracksForPage(page: Int = 1, isRefresh: Boolean = false, isPaging: Boolean) {
        if (isRefresh) {
            isInit = true
            totalScrollY = 0
            binding.vScrollCover.visibility = View.INVISIBLE
        }
        // 加载开始
        viewModel.loadSongs(page, isRefresh, isPaging)
    }

    private fun createTrackSelectView(trackTag: List<TrackTag>) {
        binding.flAlbumTrackList.removeAllViews()
        trackTag.forEachIndexed { index, tag ->
            val tv = TextView(context)
            tv.text = tag.tagName
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
            lp.topMargin = resources.getDimension(R.dimen._16dp).toInt()
            lp.minWidth = resources.getDimension(R.dimen._118dp).toInt()

            selectTag(tv)

            tv.setOnClickListener { v ->
                val viewIndex = v.tag as Int
                if (viewIndex == selectedIndex) {
                    v as TextView
                    v.setTextColor(resources.getColor(R.color.color_9B9B9B))
                    v.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_album_detail_tag)
                    loadTracksForPage(isRefresh = true, isPaging = true)
                    selectedIndex = null
                } else {
                    selectedIndex = viewIndex
                    loadTracksForPage(page = tag.page, isRefresh = true, isPaging = false)
                    binding.flAlbumTrackList.children.forEach { child ->
                        selectTag(child as TextView)
                    }
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
        if (!isShowTrackSelectPanel) {
            binding.vScrollCoverTrackList.visibility = View.GONE
        } else if (currentTrackListCoverScrollY > 0) {
            binding.vScrollCoverTrackList.visibility = View.VISIBLE
        }

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

    interface OnFragmentActionListener {
        fun hideFloatWindow()
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

data class TrackTag(
    val tagName: String,
    val page: Int
)