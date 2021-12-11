package xh.zero.tadpolestory.ui.more

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentMoreUpdateBinding
import xh.zero.tadpolestory.ui.BaseFragment

class MoreUpdateFragment : BaseFragment<FragmentMoreUpdateBinding>() {



    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMoreUpdateBinding {
        return FragmentMoreUpdateBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    companion object {
        fun newInstance() = MoreUpdateFragment()
    }
}