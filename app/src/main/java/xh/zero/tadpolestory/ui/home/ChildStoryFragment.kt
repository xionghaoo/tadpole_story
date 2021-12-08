package xh.zero.tadpolestory.ui.home

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentChildStoryBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.AlbumMetaData
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.ui.MainFragmentDirections
import xh.zero.tadpolestory.ui.MainViewModel
import xh.zero.tadpolestory.ui.serach.FilterFragment.Companion.TAG_NAME_ALL
import kotlin.math.roundToInt

@AndroidEntryPoint
class ChildStoryFragment : BaseFragment<FragmentChildStoryBinding>() {

    private val viewModel: MainViewModel by viewModels()
    private var selectedIndex = 0
    //    private var isInitial = false
    private var selectedMenuPos = 0
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
    ): FragmentChildStoryBinding {
        return FragmentChildStoryBinding.inflate(layoutInflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnFilter.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToFilterFragment(TAG_NAME_ALL))
        }

        loadData()

        binding.scrollViewContent.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY - oldScrollY > 10) {
                listener?.hideFloatWindow()
            }
        }

    }

    private fun loadData() {
        viewModel.getTagList().observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
                if (r.isNotEmpty()) {
                    val tags = r.first().attributes?.toMutableList()
                    tags?.add(0, AlbumMetaData.Attributes().apply {
                        attr_key = -1
                        display_name = "排行榜"
                    })
                    bindTagList(tags)
                }
            }
        }

        viewModel.getTemporaryToken().observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
                binding.llContentList.removeAllViews()
                loadRecommend(r.access_token!!)
            }
        }
    }

    private fun loadRecommend(token: String) {
        viewModel.getDailyRecommendAlbums(token, 1).observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
                loadGuessLike()
                // TODO 数据筛选
                val items = r.albums?.filter { album -> album.category_id == 6 || album.category_id == 92 }?.filterIndexed { index, _ -> index < 4 }
                addContentItemView("每日推荐", items ?: emptyList())
            }
        }
    }

    private fun loadGuessLike() {
        viewModel.getGuessLikeAlbums().observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
                // TODO 数据筛选
//                val items = r.filter { album -> album.category_id == 6 || album.category_id == 92 }.filterIndexed { index, _ -> index < 4 }
                val items = r.filterIndexed { index, _ -> index < 4 }
                addContentItemView("猜你喜欢", items, resources.getDimension(R.dimen._30dp).toInt())
            }
        }
    }

    private fun addContentItemView(title: String, albums: List<Album>, marginBottom: Int = 0) {
        val layout = layoutInflater.inflate(R.layout.item_home_content, null)
        layout.findViewById<TextView>(R.id.tv_album_container_title).text = title
        val rcAlbumList = layout.findViewById<FlexboxLayout>(R.id.rc_album_list)
        rcAlbumList.removeAllViews()
        albums.forEach { item ->
            val v = layoutInflater.inflate(R.layout.list_item_home_album, null)
            rcAlbumList.addView(v)
            val lp = v.layoutParams as FlexboxLayout.LayoutParams
            lp.width = resources.getDimension(R.dimen._216dp).toInt()
            lp.height = resources.getDimension(R.dimen._292dp).toInt()

            v.findViewById<TextView>(R.id.tv_album_title).text = item.album_title
            v.findViewById<TextView>(R.id.tv_album_desc).text = item.album_intro
            Glide.with(v.context)
                .load(item.cover_url_large)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(v.context.resources.getDimension(R.dimen._24dp).roundToInt())))
                .into(v.findViewById(R.id.iv_album_icon))

            v.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToAlbumDetailFragment(
                    albumId = item.id,
                    totalCount = item.include_track_count,
                    albumTitle = item.album_title.orEmpty(),
                    albumCover = item.cover_url_large.orEmpty(),
                    albumDesc = item.meta.orEmpty(),
                    albumSubscribeCount = item.subscribe_count,
                    albumTags = item.album_tags.orEmpty(),
                    albumIntro = item.album_intro.orEmpty()
                ))
            }
        }
        binding.llContentList.addView(layout)
        val layoutLp = layout.layoutParams as LinearLayout.LayoutParams
        layoutLp.bottomMargin = marginBottom
    }

    private fun bindTagList(tags: List<AlbumMetaData.Attributes?>?) {
        if (tags == null) return
        binding.llTagList.removeAllViews()
        tags.forEachIndexed { index, tag ->
            val tv = TextView(requireContext())
            tv.text = tag?.display_name
            tv.tag = index
            val padding = resources.getDimension(R.dimen._28dp).toInt()
            tv.gravity = Gravity.CENTER
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._22sp))
            tv.setPadding(padding, 0, padding, 0)
            binding.llTagList.addView(tv)
            val lp = tv.layoutParams as LinearLayout.LayoutParams
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT
            lp.height = LinearLayout.LayoutParams.MATCH_PARENT
            lp.marginEnd = resources.getDimension(R.dimen._16dp).toInt()

            if (index == selectedIndex) {
                tv.setBackgroundResource(R.drawable.shape_album_tag_selected)
                tv.setTextColor(resources.getColor(R.color.white))
            } else {
                tv.setBackgroundResource(R.drawable.shape_album_tag)
                tv.setTextColor(resources.getColor(R.color.color_42444B))
            }

            selectTagView(tv)

            tv.setOnClickListener { v ->
                val viewIndex = v.tag as Int
                if (viewIndex == 0) {
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToRankFragment())
                } else {
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToFilterFragment(tag?.display_name ?: TAG_NAME_ALL))
                }

//                binding.llTagList.children.forEach { child ->
//                    selectTagView(child as TextView)
//                }
            }
        }
    }

    private fun selectTagView(v: TextView) {
        val tagIndex =  v.tag as Int
        v.apply {
            if (tagIndex == selectedIndex) {
                setBackgroundResource(R.drawable.shape_album_tag_selected)
                setTextColor(resources.getColor(R.color.white))
            } else {
                setBackgroundResource(R.drawable.shape_album_tag)
                setTextColor(resources.getColor(R.color.color_42444B))
            }
        }
    }

    interface OnFragmentActionListener {
        fun hideFloatWindow()
    }

    companion object {
        fun newInstance() = ChildStoryFragment()
    }
}