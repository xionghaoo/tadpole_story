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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.Configs
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

/**
 * 儿童故事/少儿素养
 */
@AndroidEntryPoint
class ChildStoryFragment : BaseFragment<FragmentChildStoryBinding>() {

    private val viewModel: MainViewModel by viewModels()
    private var listener: OnFragmentActionListener? = null
    private val categoryId: Int by lazy {
        arguments?.getInt(ARG_CATEGORY_ID, 0) ?: 0
    }

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
            toFilterPage(TAG_NAME_ALL)
        }

        binding.scrollViewContent.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY - oldScrollY > 10) {
                listener?.hideFloatWindow()
            }
        }

        binding.vSearch.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSearchFragment(categoryId))
        }

        initial()
    }

    fun initial() {
        viewModel.getTagList(categoryId).observe(this) {
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

        viewModel.getTemporaryToken().observe(this) {
            handleResponse(it) { r ->
                viewModel.repo.prefs.accessToken = r.access_token
//                binding.llContentList.removeAllViews()
                loadRecommend(r.access_token!!)
            }
        }

//        if (viewModel.repo.prefs.accessToken == null) {
//            viewModel.getTemporaryToken().observe(this) {
//                handleResponse(it) { r ->
//                    viewModel.repo.prefs.accessToken = r.access_token
//                    binding.llContentList.removeAllViews()
//                    loadRecommend(r.access_token!!)
//                }
//            }
//        } else {
//            loadRecommend(viewModel.repo.prefs.accessToken!!)
//        }
    }

    /**
     * 每日推荐
     */
    private fun loadRecommend(token: String) {
        viewModel.getDailyRecommendAlbums(token, 1).observe(this) {
            handleResponse(it) { r ->
                val items = r.albums
                    ?.filter { album -> album.category_id == categoryId }
                    ?.filterIndexed { index, _ -> index < 4 }
                    ?: emptyList()
                if (items.size < 4) {
                    loadSupplementAlbums(items.toMutableList())
                } else {
                    loadGuessLike()
                    addContentItemView(0, items)
                }
            }
        }
    }

    /**
     * 每日推荐不够四个时的补充专辑
     */
    private fun loadSupplementAlbums(items: MutableList<Album>) {
        viewModel.getAlbumList(1, categoryId, if (categoryId == Configs.CATEGORY_ID_STORY) "科普" else "自然科普").observe(this) {
            handleResponse(it) { r ->
                if (r.albums?.isNotEmpty() == true) {
                    for (i in items.size.until(4)) {
                        items.add(r.albums[i - items.size])
                    }
                }
                loadGuessLike()
                addContentItemView(0, items)
            }
        }
    }

    /**
     * 猜你喜欢
     */
    private fun loadGuessLike() {
        viewModel.getGuessLikeAlbums().observe(this) {
            handleResponse(it) { r ->
                // TODO 数据筛选
                val items = r.filterIndexed { index, _ -> index < 4 }
                addContentItemView(1, items, resources.getDimension(R.dimen._30dp).toInt())
            }
        }
    }

    private fun addContentItemView(contentIndex: Int, albums: List<Album>, marginBottom: Int = 0) {
        val layout = if (binding.llContentList.childCount < 2) {
            val contentLayout = layoutInflater.inflate(R.layout.item_home_content, null)
            binding.llContentList.addView(contentLayout)
            contentLayout
        } else {
            binding.llContentList.getChildAt(contentIndex)
        }
        layout.findViewById<TextView>(R.id.tv_album_container_title).text = if (contentIndex == 0) "每日推荐" else "猜你喜欢"
        val rcAlbumList = layout.findViewById<FlexboxLayout>(R.id.rc_album_list)
        layout.findViewById<View>(R.id.btn_more).setOnClickListener {
            if (contentIndex == 0) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToDayRecommendFragment(categoryId))
            } else {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToGuessLikeFragment(categoryId))
            }
        }
        // 换一批
        if (contentIndex == 0) {
            layout.findViewById<View>(R.id.btn_refresh).visibility = View.GONE
        } else {
            layout.findViewById<View>(R.id.btn_refresh).setOnClickListener {
                if (contentIndex == 1) {
                    loadGuessLike()
                }
            }
        }
        albums.forEachIndexed { index, item ->
            val v: View = if (rcAlbumList.childCount < 4) {
                val view = layoutInflater.inflate(R.layout.list_item_home_album, null)
                rcAlbumList.addView(view)
                view
            } else {
                rcAlbumList.getChildAt(index)
            }
            val lp = v.layoutParams as FlexboxLayout.LayoutParams
            lp.width = resources.getDimension(R.dimen._216dp).toInt()
            lp.height = resources.getDimension(R.dimen._292dp).toInt()
            if (index > 0) {
                lp.leftMargin = resources.getDimension(R.dimen._24dp).toInt()
            }

            v.findViewById<TextView>(R.id.tv_album_title).text = item.album_title
            v.findViewById<TextView>(R.id.tv_album_desc).text = item.album_intro
            Glide.with(v.context)
                .load(item.cover_url_large)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(v.context.resources.getDimension(R.dimen._24dp).roundToInt())))
                .into(v.findViewById(R.id.iv_album_icon))

            v.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToAlbumDetailFragment(
                    album = item
                ))
            }
        }
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

            tv.setBackgroundResource(R.drawable.shape_album_tag)
            tv.setTextColor(resources.getColor(R.color.color_42444B))

            tv.setOnClickListener { v ->
                val viewIndex = v.tag as Int
                if (viewIndex == 0) {
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToRankFragment(categoryId))
                } else {
                    toFilterPage(tag?.display_name ?: TAG_NAME_ALL)
                }
            }
        }
    }

//    private fun selectTagView(v: TextView) {
//        val tagIndex =  v.tag as Int
//        v.apply {
//            if (tagIndex == selectedIndex) {
//                setBackgroundResource(R.drawable.shape_album_tag_selected)
//                setTextColor(resources.getColor(R.color.white))
//            } else {
//                setBackgroundResource(R.drawable.shape_album_tag)
//                setTextColor(resources.getColor(R.color.color_42444B))
//            }
//        }
//    }

    private fun toFilterPage(tag: String) {
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToFilterFragment(
            tagName = tag,
            categoryId = categoryId
        ))
    }

    interface OnFragmentActionListener {
        fun hideFloatWindow()
    }

    companion object {
        private const val ARG_CATEGORY_ID = "ARG_CATEGORY_ID"

        fun newInstance(categoryId: Int) = ChildStoryFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_CATEGORY_ID, categoryId)
            }
        }
    }
}