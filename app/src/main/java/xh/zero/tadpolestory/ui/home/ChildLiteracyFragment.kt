package xh.zero.tadpolestory.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentChildLiteracyBinding
import xh.zero.tadpolestory.ui.BaseFragment

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

    }
    companion object {

        fun newInstance() = ChildLiteracyFragment()
    }
}