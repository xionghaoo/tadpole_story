package xh.zero.tadpolestory.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentChildLiteracyBinding
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.ui.MainFragmentDirections
import xh.zero.tadpolestory.ui.serach.FilterFragment

/**
 * 少儿素养
 */
class ChildLiteracyFragment : BaseFragment<FragmentChildLiteracyBinding>() {

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
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToFilterFragment(
                    FilterFragment.TAG_NAME_ALL
                ))
        }

//        loadData()

        binding.scrollViewContent.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY - oldScrollY > 10) {
//                listener?.hideFloatWindow()
            }
        }

        binding.vSearch.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSearchFragment())
        }
    }
    companion object {

        fun newInstance() = ChildLiteracyFragment()
    }
}