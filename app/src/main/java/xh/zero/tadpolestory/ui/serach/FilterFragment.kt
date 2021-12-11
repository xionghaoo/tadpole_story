package xh.zero.tadpolestory.ui.serach

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentFilterBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.repo.data.AlbumMetaData
import xh.zero.tadpolestory.ui.BaseFragment
import java.lang.StringBuilder

@AndroidEntryPoint
class FilterFragment : BaseFragment<FragmentFilterBinding>() {

    private val viewModel: FilterViewModel by viewModels()
    private lateinit var adapter: FilterAlbumAdapter
    private var selectedTagIndexMap = HashMap<Int, FilterItem>()
//    private var filterMap = HashMap<Int, AlbumMetaData.Attributes>()
    private var panelIsShow = true
    private val args: FilterFragmentArgs by navArgs()

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentFilterBinding {
        return FragmentFilterBinding.inflate(layoutInflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.tvPageTitle.text = "儿童故事"

        binding.rcAlbumList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && panelIsShow) {
                    showFilterPanel(false)
                }
            }
        })

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
                    albumIntro = album.album_intro.orEmpty(),
                    albumRichInfo = album.short_rich_intro.orEmpty()
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
//            when (it.status) {
//                Status.LOADING -> {
//
//                }
//                Status.SUCCESS -> {
//
//                }
//                Status.ERROR -> {
//
//                }
//            }
        }
        viewModel.refreshState.observe(viewLifecycleOwner) {

        }

        binding.btnFilterExpand.findViewById<TextView>(R.id.tv_filter_expand_title).text = "收起"
        binding.btnFilterExpand.setOnClickListener {
            showFilterPanel(!panelIsShow)
        }

        loadMeta()
        if (args.tagName == TAG_NAME_ALL) {
            loadMetaAlbums()
        }

    }

    private fun showFilterPanel(isShow: Boolean) {
        panelIsShow = isShow
        val tvTitle = binding.btnFilterExpand.findViewById<TextView>(R.id.tv_filter_expand_title)
        binding.containerFilterOther.visibility = if (!isShow) {
            tvTitle.text = "展开"
            View.GONE
        } else {
            tvTitle.text = "收起"
            View.VISIBLE
        }
    }

    private fun loadMeta() {
        viewModel.getMetadataList().observe(this, Observer {
            handleResponse(it) { r ->
                if (r.isNotEmpty()) {
                    val topTags = ArrayList<AlbumMetaData.Attributes>()
                    topTags.add(AlbumMetaData.Attributes().apply {
                        attr_key = -1
                        display_name = TAG_NAME_ALL
                    })
                    topTags.addAll(r.first().attributes ?: emptyList())
                    bindTopTagList(0, topTags)

                    if (r.size > 1) {
                        // "综合排序", "播放最多", "最近更新"
                        createFilterTags(1, binding.containerFilterCalc, listOf(
                            AlbumMetaData.Attributes().apply {
                                attr_key = 0
                                attr_value = "1"
                                display_name = "综合排序"
                            },
                            AlbumMetaData.Attributes().apply {
                                attr_key = 0
                                attr_value = "2"
                                display_name = "播放最多"
                            },
                            AlbumMetaData.Attributes().apply {
                                attr_key = 0
                                attr_value = "3"
                                display_name = "最近更新"
                            }
                        ), false)

                        bindFilterOtherTags(r.subList(1, r.size).map { meta -> meta.attributes ?: emptyList() }.toList())
                    }
                }
            }
        })
    }

    private fun loadMetaAlbums() {
        val attrsQuery = StringBuilder()
        var calcDimen = 1
        selectedTagIndexMap.values.forEach { item ->
            if (item.attrs.attr_key == 0) {
                calcDimen = item.attrs.attr_value?.toInt() ?: 1
            } else if (item.attrs.attr_key > 0) {
                attrsQuery.append("${item.attrs.attr_key}:${item.attrs.attr_value}").append(";")
            }
        }
        Timber.d("loadMetaAlbums: ${attrsQuery}, ${calcDimen}")

        viewModel.showList(listOf(attrsQuery.toString(), calcDimen.toString()))
    }

//    private fun filterLoad(filterIndex: Int, attrs: AlbumMetaData.Attributes) {
////        filterMap[filterIndex] = attrs
//        loadMetaAlbums()
//    }

    private fun bindTopTagList(filterIndex: Int, tags: List<AlbumMetaData.Attributes?>) {
        binding.llTagList.removeAllViews()
        tags.forEachIndexed { index, tag ->
            if (tag != null) {
                val tv = TextView(requireContext())
                tv.text = tag.display_name
                tv.tag = FilterItem(index, tag)
                val padding = resources.getDimension(R.dimen._28dp).toInt()
                tv.gravity = Gravity.CENTER
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._22sp))
                tv.setPadding(padding, 0, padding, 0)
                binding.llTagList.addView(tv)
                val lp = tv.layoutParams as LinearLayout.LayoutParams
                lp.width = LinearLayout.LayoutParams.WRAP_CONTENT
                lp.height = LinearLayout.LayoutParams.MATCH_PARENT
                lp.marginEnd = resources.getDimension(R.dimen._16dp).toInt()

                if (args.tagName == TAG_NAME_ALL) {
                    if (index == 0) {
                        selectedTagIndexMap[filterIndex] = FilterItem(0, tag)

                        tv.setBackgroundResource(R.drawable.shape_album_tag_selected)
                        tv.setTextColor(resources.getColor(R.color.white))
                    } else {
                        tv.setBackgroundResource(R.drawable.shape_album_tag)
                        tv.setTextColor(resources.getColor(R.color.color_42444B))
                    }
                } else {
                    // 选中tag，并加载数据
                    if (tag.display_name == args.tagName) {
                        selectedTagIndexMap[filterIndex] = FilterItem(index, tag)
                        selectTopTagView(filterIndex, tv, index)
                        binding.containerLlTagList.post {
                            binding.containerLlTagList.smoothScrollTo(binding.llTagList.getChildAt(index).x.toInt(), 0)
                        }
                        loadMetaAlbums()
                    } else {
                        tv.setBackgroundResource(R.drawable.shape_album_tag)
                        tv.setTextColor(resources.getColor(R.color.color_42444B))
                    }
                }

                tv.setOnClickListener { v ->
                    val item = v.tag as FilterItem
                    selectedTagIndexMap[filterIndex] = item
//                    loadMetaAlbums()
//                    filterLoad(filterIndex, item.attrs)
                    loadMetaAlbums()
                    binding.llTagList.children.forEachIndexed { index, child ->
                        selectTopTagView(filterIndex, child as TextView, index)
                    }
                }
            }
        }
    }

    private fun createFilterTags(filterIndex: Int, container: ViewGroup, tags: List<AlbumMetaData.Attributes?>?, hasAll: Boolean = true) {
        if (tags == null) return
        container.removeAllViews()
        val newTags = ArrayList<AlbumMetaData.Attributes?>()
        if (hasAll) {
            newTags.add(AlbumMetaData.Attributes().apply {
                attr_key = -1
                display_name = "全部"
            })
            newTags.addAll(tags)
        } else {
            newTags.addAll(tags)
        }
        newTags.forEachIndexed { index, tag ->
            if (tag != null) {
                val tv = TextView(requireContext())
                tv.text = tag.display_name
                tv.tag = FilterItem(index, tag)
                val padding = resources.getDimension(R.dimen._19dp).toInt()
                tv.gravity = Gravity.CENTER
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._20sp))
                tv.setPadding(padding, 0, padding, 0)
                container.addView(tv)
                val lp = tv.layoutParams as LinearLayout.LayoutParams
                lp.width = LinearLayout.LayoutParams.WRAP_CONTENT
                lp.height = LinearLayout.LayoutParams.MATCH_PARENT
                lp.marginEnd = resources.getDimension(R.dimen._8dp).toInt()

                // 初始化
                if (index == 0) {
                    selectedTagIndexMap[filterIndex] = FilterItem(0, tag)

                    tv.setBackgroundResource(R.drawable.shape_filter_tag_selected)
                    tv.setTextColor(resources.getColor(R.color.color_FF9F00))
                } else {
                    tv.background = null
                    tv.setTextColor(resources.getColor(R.color.color_9B9B9B))
                }

                tv.setOnClickListener { v ->
                    val item = v.tag as FilterItem
                    selectedTagIndexMap[filterIndex] = item
//                    filterLoad(filterIndex, item.attrs)
                    loadMetaAlbums()
                    container.children.forEachIndexed { index, child ->
                        selectFilterTagView(filterIndex, child as TextView, index)
                    }
                }
            }
        }
    }

    private fun bindFilterOtherTags(tagsList: List<List<AlbumMetaData.Attributes?>>?) {
        if (tagsList == null) return
        binding.containerFilterOther.removeAllViews()
        tagsList.forEachIndexed { filterIndex, tags ->
            val ll = LinearLayout(context)
            ll.orientation = LinearLayout.HORIZONTAL
            val scrollView = HorizontalScrollView(context)
            scrollView.isHorizontalScrollBarEnabled = false
            binding.containerFilterOther.addView(scrollView)
            scrollView.addView(ll)
            ll.layoutParams.height = resources.getDimension(R.dimen._42dp).toInt()
            val lp = scrollView.layoutParams as LinearLayout.LayoutParams
            lp.topMargin = resources.getDimension(R.dimen._16dp).toInt()
            createFilterTags(filterIndex + 2, ll, tags)
        }

    }

    private fun selectTopTagView(filterIndex: Int, v: TextView, childIndex: Int) {
//        val attr =  v.tag as FilterItem
        val selectedIndex = selectedTagIndexMap[filterIndex]?.index
        v.apply {
            if (childIndex == selectedIndex) {
                setBackgroundResource(R.drawable.shape_album_tag_selected)
                setTextColor(resources.getColor(R.color.white))
            } else {
                setBackgroundResource(R.drawable.shape_album_tag)
                setTextColor(resources.getColor(R.color.color_42444B))
            }
        }
    }

    private fun selectFilterTagView(filterIndex: Int, v: TextView, childIndex: Int) {
//        val attr =  v.tag as FilterItem
        val selectedIndex = selectedTagIndexMap[filterIndex]?.index
        v.apply {
            if (childIndex == selectedIndex) {
                setBackgroundResource(R.drawable.shape_filter_tag_selected)
                setTextColor(resources.getColor(R.color.color_FF9F00))
            } else {
                background = null
                setTextColor(resources.getColor(R.color.color_9B9B9B))
            }
        }
    }

    companion object {
        const val TAG_NAME_ALL = "全部"
    }
}

data class FilterItem(
    val index: Int,
    val attrs: AlbumMetaData.Attributes
)