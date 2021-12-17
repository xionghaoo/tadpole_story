package xh.zero.tadpolestory.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentMainBinding
import xh.zero.tadpolestory.ui.home.ChildStoryFragment
import xh.zero.tadpolestory.ui.more.MoreFragment
import java.lang.IllegalArgumentException

@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>() {

    private lateinit var storyFragment: ChildStoryFragment
    private lateinit var literacyFragment: ChildStoryFragment
    private lateinit var moreFragment: MoreFragment

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMainBinding = FragmentMainBinding.inflate(layoutInflater, container, false)

    override fun rootView(): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        storyFragment = ChildStoryFragment.newInstance(Configs.CATEGORY_ID_STORY)
        literacyFragment = ChildStoryFragment.newInstance(Configs.CATEGORY_ID_LITERACY)
        moreFragment = MoreFragment.newInstance()

        binding.btnHome.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.btnHomeMenu1.setOnClickListener {
            if (binding.viewPager.currentItem == 0) return@setOnClickListener
            binding.btnHomeMenu1.setImageResource(R.mipmap.ic_home_menu_1_selected)
            binding.btnHomeMenu2.setImageResource(R.mipmap.ic_home_menu_2)
            binding.btnHomeMenu3.setImageResource(R.mipmap.ic_home_menu_3)
            binding.viewPager.setCurrentItem(0, false)
            if (storyFragment.isAdded) {
                storyFragment.initial()
            }
        }
        binding.btnHomeMenu2.setOnClickListener {
            if (binding.viewPager.currentItem == 1) return@setOnClickListener
            binding.btnHomeMenu1.setImageResource(R.mipmap.ic_home_menu_1)
            binding.btnHomeMenu2.setImageResource(R.mipmap.ic_home_menu_2_selected)
            binding.btnHomeMenu3.setImageResource(R.mipmap.ic_home_menu_3)
            binding.viewPager.setCurrentItem(1, false)

            if (literacyFragment.isAdded) {
                literacyFragment.initial()
            }
        }
        binding.btnHomeMenu3.setOnClickListener {
            if (binding.viewPager.currentItem == 2) return@setOnClickListener
            binding.btnHomeMenu1.setImageResource(R.mipmap.ic_home_menu_1)
            binding.btnHomeMenu2.setImageResource(R.mipmap.ic_home_menu_2)
            binding.btnHomeMenu3.setImageResource(R.mipmap.ic_home_menu_3_selected)
            binding.viewPager.setCurrentItem(2, false)

            if (moreFragment.isAdded) {
                moreFragment.initial()
            }
        }
        binding.viewPager.adapter = MainPageAdapter()
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewPager.isSaveEnabled = false

        binding.btnHomeMenu1.performClick()

    }

    private inner class MainPageAdapter : FragmentStateAdapter(childFragmentManager, lifecycle) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> storyFragment
                1 -> literacyFragment
                2 -> moreFragment
                else -> throw IllegalArgumentException("非法位置")
            }
        }

    }

}