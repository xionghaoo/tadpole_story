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
            onTextChanged = { text, start, before, count ->
                val q = text?.toString() ?: ""
                if (q.isEmpty()) {
                    binding.containerSearchRecommend.visibility = View.VISIBLE
                } else {
                    binding.containerSearchRecommend.visibility = View.GONE
                    job?.cancel()
                    job = CoroutineScope(Dispatchers.Default).launch {
                        delay(50)
                        withContext(Dispatchers.Main) {
                            searchAlbums(q)
                        }
                    }
                }
            }
        )

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
    }

    private fun searchAlbums(txt: String) {
        viewModel.showList(listOf(txt))
    }

    private fun getHotKeyword() {
        viewModel.getHotKeyword().observe(viewLifecycleOwner) {
            handleResponse(it) { tags ->
                bindHotTags(tags)
            }
        }
    }

    private fun bindHotTags(tags: List<HotKeyword>) {
        binding.llHotTags.removeAllViews()
        tags.forEach { tag ->
            val tv = TextView(context)
            binding.llHotTags.addView(tv)
            tv.text = tag.search_word
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
        }
    }
}