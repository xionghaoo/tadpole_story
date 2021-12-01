package xh.zero.tadpolestory.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentMainBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.ui.album.AlbumDetailFragment
import xh.zero.tadpolestory.ui.album.AlbumDetailFragmentArgs
import xh.zero.tadpolestory.ui.home.RecommendAlbumAdapter
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>() {

//    private var _binding: FragmentMainBinding? = null
//    private val binding: FragmentMainBinding get() = _binding!!
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
    ): FragmentMainBinding = FragmentMainBinding.inflate(layoutInflater, container, false)

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnHome.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.btnHomeMenu1.setOnClickListener {
            selectedMenuPos = 0
        }
        binding.btnHomeMenu1.setOnClickListener {
            selectedMenuPos = 1
        }
        binding.btnHomeMenu1.setOnClickListener {
            selectedMenuPos = 2
        }

        binding.btnFilter.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToFilterFragment())
        }

        loadData()

        binding.scrollViewContent.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            Timber.d("setOnScrollChangeListener: $scrollY, $oldScrollY")
            if (scrollY - oldScrollY > 10) {
                listener?.hideFloatWindow()
            }
        }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        if (!isInitial) return
//        isInitial = false
//        Timber.d("onViewCreated")
//        binding.btnHome.setOnClickListener {
//            activity?.onBackPressed()
//        }
//
//        binding.btnHomeMenu1.setOnClickListener {
//            selectedMenuPos = 0
//        }
//        binding.btnHomeMenu1.setOnClickListener {
//            selectedMenuPos = 1
//        }
//        binding.btnHomeMenu1.setOnClickListener {
//            selectedMenuPos = 2
//        }
//
//        binding.btnFilter.setOnClickListener {
//
//        }
//
//        loadData()
//    }

    private fun loadData() {
        viewModel.getTagList().observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
                bindTagList(r.map { tag -> tag.tag_name })
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
                    totalCount = item.include_track_count.toInt(),
                    albumTitle = item.album_title.orEmpty(),
                ))
            }
        }
        binding.llContentList.addView(layout)
        val layoutLp = layout.layoutParams as LinearLayout.LayoutParams
        layoutLp.bottomMargin = marginBottom
    }

    private fun bindTagList(tags: List<String?>) {
        binding.llTagList.removeAllViews()
        tags.forEachIndexed { index, tag ->
            val tv = TextView(requireContext())
            tv.text = tag
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
                selectedIndex = v.tag as Int

                binding.llTagList.children.forEach { child ->
                    selectTagView(child as TextView)
                }
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
        fun newInstance() = MainFragment()
    }
}