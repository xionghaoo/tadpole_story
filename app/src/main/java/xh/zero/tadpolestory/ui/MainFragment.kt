package xh.zero.tadpolestory.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentMainBinding
import xh.zero.tadpolestory.replaceFragment
import xh.zero.tadpolestory.ui.home.ChildLiteracyFragment
import xh.zero.tadpolestory.ui.home.ChildStoryFragment

@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>() {

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
        binding.btnHome.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.btnHomeMenu1.setOnClickListener {
            binding.btnHomeMenu1.setImageResource(R.mipmap.ic_home_menu_1_selected)
            binding.btnHomeMenu2.setImageResource(R.mipmap.ic_home_menu_2)
            binding.btnHomeMenu3.setImageResource(R.mipmap.ic_home_menu_3)
            binding.viewPager.setCurrentItem(0, false)
        }
        binding.btnHomeMenu2.setOnClickListener {
            binding.btnHomeMenu1.setImageResource(R.mipmap.ic_home_menu_1)
            binding.btnHomeMenu2.setImageResource(R.mipmap.ic_home_menu_2_selected)
            binding.btnHomeMenu3.setImageResource(R.mipmap.ic_home_menu_3)
            binding.viewPager.setCurrentItem(1, false)
        }
        binding.btnHomeMenu3.setOnClickListener {
            binding.btnHomeMenu1.setImageResource(R.mipmap.ic_home_menu_1)
            binding.btnHomeMenu2.setImageResource(R.mipmap.ic_home_menu_2)
            binding.btnHomeMenu3.setImageResource(R.mipmap.ic_home_menu_3_selected)
            binding.viewPager.setCurrentItem(2, false)
        }

        binding.btnHomeMenu1.performClick()

        binding.viewPager.adapter = MainPageAdapter()
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewPager.isSaveEnabled = false
    }

    private inner class MainPageAdapter : FragmentStateAdapter(childFragmentManager, lifecycle) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ChildStoryFragment.newInstance()
                1 -> ChildLiteracyFragment.newInstance()
                else -> ChildLiteracyFragment.newInstance()
            }
        }

    }

}