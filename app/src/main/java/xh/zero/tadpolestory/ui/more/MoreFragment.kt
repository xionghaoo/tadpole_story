package xh.zero.tadpolestory.ui.more

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import xh.zero.tadpolestory.databinding.FragmentMoreBinding
import xh.zero.tadpolestory.ui.BaseFragment

class MoreFragment : BaseFragment<FragmentMoreBinding>() {

    private val titles = arrayOf("订阅", "播放历史", "版本更新")
    private lateinit var adapter: MoreAdapter

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMoreBinding {
        return FragmentMoreBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = MoreAdapter()
        binding.vpMore.adapter = adapter
        binding.tlMore.setViewPager(binding.vpMore, titles)
//        binding.vpMore.clearOnPageChangeListeners()
//        binding.tlMore.setOnTabClickListener { pos ->
//            binding.vpMore.setCurrentItem(pos, false)
//        }
        binding.vpMore.setOnTouchListener { v, event -> true }


    }

//    private inner class MoreAdapter : FragmentPagerAdapter(childFragmentManager,  BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
//
//        override fun getCount(): Int = titles.size
//
//        override fun getItem(position: Int): Fragment {
//            return when(position) {
//                0 -> MoreSubscribeFragment.newInstance(position)
//                1 -> MoreHistoryFragment.newInstance()
//                else -> MoreHistoryFragment.newInstance()
//            }
//        }
//
//        override fun getPageTitle(position: Int): CharSequence? {
//            return titles[position]
//        }
//    }

    private inner class MoreAdapter : FragmentStateAdapter(this) {

        override fun getItemCount(): Int = titles.size

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> MoreSubscribeFragment.newInstance(position)
                1 -> MoreHistoryFragment.newInstance()
                else -> MoreHistoryFragment.newInstance()
            }
        }

    }

    companion object {
        fun newInstance() = MoreFragment()
    }

}