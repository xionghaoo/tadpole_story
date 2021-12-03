package xh.zero.tadpolestory.ui.more

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentMoreHistoryBinding
import xh.zero.tadpolestory.ui.BaseFragment

class MoreHistoryFragment : BaseFragment<FragmentMoreHistoryBinding>() {

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMoreHistoryBinding {
        return FragmentMoreHistoryBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    companion object {

        fun newInstance() = MoreHistoryFragment()
    }
}