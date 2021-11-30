package xh.zero.tadpolestory.ui.serach

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentFilterBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.ui.BaseFragment

@AndroidEntryPoint
class FilterFragment : BaseFragment<FragmentFilterBinding>() {

    private val viewModel: SearchViewModel by viewModels()
    private var selectedIndex = 0
    private lateinit var adapter: FilterAlbumAdapter

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

        loadData()

        binding.rcAlbumsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 5) {
                    binding.containerSearchTagsList.visibility = View.GONE
                } else if (dy < -5) {
                    binding.containerSearchTagsList.visibility = View.VISIBLE
                }
            }
        })

        binding.rcAlbumsList.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = FilterAlbumAdapter()
        binding.rcAlbumsList.adapter = adapter
    }

    private fun loadData() {
        viewModel.getTagList().observe(viewLifecycleOwner) {
            handleResponse(it) { r ->
                bindTagList(r.map { tag -> tag.tag_name })
            }
        }
        viewModel.searchAlbums(1, "3-6岁,睡前").observe(viewLifecycleOwner) {
            handleResponse(it) {r ->
                adapter.updateData(r.albums ?: emptyList())
            }
        }

//        viewModel.getMetadataList().observe(this, Observer {
//            handleResponse(it) { r ->
//
//            }
//        })
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

}