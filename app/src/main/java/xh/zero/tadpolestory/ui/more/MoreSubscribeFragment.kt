package xh.zero.tadpolestory.ui.more

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentMoreSubscribeBinding
import xh.zero.tadpolestory.ui.BaseFragment

class MoreSubscribeFragment : BaseFragment<FragmentMoreSubscribeBinding>() {

    private var selectedIndex = 0

    private val position: Int by lazy {
        arguments?.getInt(ARG_POSITION, 0) ?: 0
    }

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMoreSubscribeBinding {
        return FragmentMoreSubscribeBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        bindTagList(arrayListOf("最近常听", "最近更新", "最新订阅"))
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

    companion object {
        private const val ARG_POSITION = "ARG_POSITION"

        fun newInstance(position: Int) = MoreSubscribeFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POSITION, position)
            }
        }
    }
}