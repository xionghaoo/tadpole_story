package xh.zero.tadpolestory.ui.home

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentChildLiteracyBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.repo.data.AlbumMetaData
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.ui.MainFragmentDirections
import xh.zero.tadpolestory.ui.MainViewModel
import xh.zero.tadpolestory.ui.serach.FilterFragment

/**
 * 少儿素养
 */
@AndroidEntryPoint
class ChildLiteracyFragment : BaseFragment<FragmentChildLiteracyBinding>() {

    private val viewModel: MainViewModel by viewModels()
    private var selectedIndex = 0

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentChildLiteracyBinding {
        return FragmentChildLiteracyBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnFilter.setOnClickListener {
            toFilterPage(FilterFragment.TAG_NAME_ALL)
        }

        loadData()

        binding.scrollViewContent.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY - oldScrollY > 10) {
//                listener?.hideFloatWindow()
            }
        }

        binding.vSearch.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSearchFragment(Configs.CATEGORY_ID_EDU))
        }
    }

    private fun loadData() {
        viewModel.getTagList(Configs.CATEGORY_ID_EDU).observe(this) {
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
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToRankFragment(Configs.CATEGORY_ID_EDU))
                } else {
                    toFilterPage(tag?.display_name ?: FilterFragment.TAG_NAME_ALL)
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

    private fun toFilterPage(tag: String) {
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToFilterFragment(
                tag,
                Configs.CATEGORY_ID_EDU
            ))
    }

    companion object {

        fun newInstance() = ChildLiteracyFragment()
    }
}