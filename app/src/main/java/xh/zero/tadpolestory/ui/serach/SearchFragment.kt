package xh.zero.tadpolestory.ui.serach

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentSearchBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.HotKeyword
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.utils.PromptDialog
import xh.zero.tadpolestory.utils.TadpoleUtil

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    private val viewModel: SearchViewModel by viewModels()
    private var job: Job? = null

    private lateinit var adapter: FilterAlbumAdapter
//    private lateinit var hotAlbumsAdapter: HotAlbumAdapter
    private lateinit var searchWordAdapter: SearchWordAdapter
    private var lastQueryWord: String? = null

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener {
            back()
        }

        binding.edtSearch.addTextChangedListener(
            onTextChanged = { text, _, _, _ ->
                val q = text?.toString() ?: ""
                if (lastQueryWord == q) return@addTextChangedListener
                lastQueryWord = q
                if (q.isEmpty()) {
                    binding.containerSearchRecommend.visibility = View.VISIBLE
                    binding.rcSearchWords.visibility = View.GONE
                    binding.ivClear.visibility = View.INVISIBLE
                    binding.rcAlbumList.visibility = View.GONE
                } else {
                    binding.containerSearchRecommend.visibility = View.GONE
                    binding.rcSearchWords.visibility = View.VISIBLE
                    binding.ivClear.visibility = View.VISIBLE
                    binding.rcAlbumList.visibility = View.GONE
                    job?.cancel()
                    job = CoroutineScope(Dispatchers.Default).launch {
                        delay(50)
                        withContext(Dispatchers.Main) {
                            getSearchWords(q)
                        }
                    }
                }
            }
        )
        binding.ivClear.setOnClickListener {
            binding.edtSearch.text.clear()
        }
        binding.btnSearch.setOnClickListener {
            searchAlbums()
        }

        binding.ivClearSearchHistory.setOnClickListener {
            PromptDialog.showCommon(
                context = requireContext(),
                title = "删除",
                message = "确定要删除所有搜索历史吗？",
                onConfirm = {
                    viewModel.clearSearchHistory()
                }
            )
        }

        binding.rcAlbumList.layoutManager = LinearLayoutManager(context)
        adapter = FilterAlbumAdapter(
            onItemClick = { album ->
                toAlbumDetailPage(album)
            },
            retry = {

            }
        )
        binding.rcAlbumList.adapter = adapter

        viewModel.itemList.observe(this) {
            adapter.submitList(it)
        }
        viewModel.networkState.observe(this) {
            adapter.setNetworkState(it)
        }
        viewModel.refreshState.observe(this) {

        }

        getHotKeyword()

        binding.rcSearchWords.layoutManager = LinearLayoutManager(context)
        searchWordAdapter = SearchWordAdapter { txt ->
            searchAlbums()
        }
        binding.rcSearchWords.adapter = searchWordAdapter

        viewModel.loadSearchRecords().observe(this) {
            val records = it.map { record -> record.keyword }
            if (records.isNotEmpty()) {
                binding.containerHistoryRecords.visibility = View.VISIBLE
                bindTagsView(binding.llHistoryRecords, records) { tag ->
                    binding.edtSearch.setText(tag)
                    searchAlbums()
                }
            } else {
                binding.containerHistoryRecords.visibility = View.GONE
            }
        }

//        hotAlbumsAdapter = HotAlbumAdapter()
//        binding.rcHotAlbums.layoutManager = GridLayoutManager(context, 2)
//        binding.rcHotAlbums.adapter = hotAlbumsAdapter
        getHotAlbums()

        binding.rcAlbumList.isSaveEnabled = true
        binding.rcSearchWords.isSaveEnabled = true
        binding.rcHotAlbums.isSaveEnabled = true

    }

    private fun searchAlbums() {
        hideKeyboard()
        binding.containerSearchRecommend.visibility = View.GONE
        binding.rcSearchWords.visibility = View.GONE
        binding.rcAlbumList.visibility = View.VISIBLE

        val query = binding.edtSearch.text.toString()
        if (query.isNotEmpty()) {
            viewModel.saveSearchRecord(query)
            viewModel.showList(listOf(query))
        }
    }

    private fun getHotKeyword() {
        viewModel.getHotKeyword().observe(this) {
            handleResponse(it) { tags ->
                bindTagsView(binding.llHotTags, tags.map { word -> word.search_word ?: "" }) { tag ->
                    binding.edtSearch.setText(tag)
                    searchAlbums()
                }
            }
        }
    }

    private fun bindTagsView(container: ViewGroup, tags: List<String>, onClick: (String) -> Unit) {
        container.removeAllViews()
        tags.forEach { tag ->
            val tv = TextView(context)
            container.addView(tv)
            tv.text = tag
            tv.gravity = Gravity.CENTER
            tv.setTextColor(resources.getColor(R.color.color_9B9B9B))
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._20dp))
            tv.setBackgroundResource(R.drawable.shape_album_detail_tag)
            tv.setPadding(
                resources.getDimension(R.dimen._19dp).toInt(),
                0,
                resources.getDimension(R.dimen._19dp).toInt(),
            0
            )
            val lp = tv.layoutParams as LinearLayout.LayoutParams
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.rightMargin = resources.getDimension(R.dimen._8dp).toInt()

            tv.setOnClickListener {
                onClick(tag)
            }
        }
    }

    private fun getSearchWords(q: String) {
        viewModel.getSearchWords(q).observe(this) {
            handleResponse(it) { r ->
                searchWordAdapter.currentSearchWord = q
                searchWordAdapter.updateData(r.keywords ?: emptyList())
            }
        }
    }

    private fun getHotAlbums() {
        viewModel.getHotAlbumsList().observe(this) {
            handleResponse(it) { r ->
                bindHotAlbumsView(r.albums ?: emptyList())
            }
        }
    }

    private fun bindHotAlbumsView(items: List<Album>) {
        binding.rcHotAlbums.removeAllViews()
        items.forEach { item ->
            val v = layoutInflater.inflate(R.layout.list_item_filter, null)
            binding.rcHotAlbums.addView(v)
            bindHotAlbumItem(v, item)
            val lp = v.layoutParams as FlexboxLayout.LayoutParams
            lp.width = resources.getDimension(R.dimen._466dp).toInt()
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.bottomMargin = resources.getDimension(R.dimen._32dp).toInt()
        }
    }

    private fun bindHotAlbumItem(v: View, item: Album) {
        v.findViewById<TextView>(R.id.tv_album_title).text = item.album_title
        v.findViewById<TextView>(R.id.tv_album_desc).text = item.album_tags
        v.findViewById<TextView>(R.id.tv_album_subscribe).text = "${item.subscribe_count}"
        v.findViewById<TextView>(R.id.tv_album_total).text = "${item.include_track_count}集"
        TadpoleUtil.loadAvatar(v.context, v.findViewById(R.id.iv_album_cover), item.cover_url_large.orEmpty())

        v.setOnClickListener {
            toAlbumDetailPage(item)
        }
    }

    private fun toAlbumDetailPage(album: Album) {
        findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToAlbumDetailFragment(
            album = album
        ))
    }
}