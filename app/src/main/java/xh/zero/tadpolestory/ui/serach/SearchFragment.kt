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
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentSearchBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.repo.data.HotKeyword
import xh.zero.tadpolestory.ui.BaseFragment

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    private val viewModel: SearchViewModel by viewModels()
    private var job: Job? = null

    private lateinit var adapter: FilterAlbumAdapter
    private lateinit var searchWordAdapter: SearchWordAdapter

//    private var currentQuery: String = ""

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
            viewModel.clearSearchHistory()
        }

        binding.rcAlbumList.layoutManager = LinearLayoutManager(context)
        adapter = FilterAlbumAdapter(
            onItemClick = { album ->
                findNavController().navigate(FilterFragmentDirections.actionFilterFragmentToAlbumDetailFragment(
                    albumId = album.id,
                    albumTitle = album.album_title.orEmpty(),
                    totalCount = album.include_track_count,
                    albumCover = album.cover_url_large.orEmpty(),
                    albumDesc = album.meta.orEmpty(),
                    albumSubscribeCount = album.subscribe_count,
                    albumTags = album.album_tags.orEmpty(),
                    albumIntro = album.album_intro.orEmpty()
                ))
            },
            retry = {

            }
        )
        binding.rcAlbumList.adapter = adapter

        viewModel.itemList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.networkState.observe(viewLifecycleOwner) {
            adapter.setNetworkState(it)
        }
        viewModel.refreshState.observe(viewLifecycleOwner) {

        }

        getHotKeyword()

        binding.rcSearchWords.layoutManager = LinearLayoutManager(context)
        searchWordAdapter = SearchWordAdapter { txt ->
//            currentQuery = txt ?: ""
            searchAlbums()
        }
        binding.rcSearchWords.adapter = searchWordAdapter

        viewModel.loadSearchRecords().observe(viewLifecycleOwner) {
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
        viewModel.getHotKeyword().observe(viewLifecycleOwner) {
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
//        currentQuery = q
        viewModel.getSearchWords(q).observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
                searchWordAdapter.updateData(r.keywords ?: emptyList())
            }
        }
    }
}