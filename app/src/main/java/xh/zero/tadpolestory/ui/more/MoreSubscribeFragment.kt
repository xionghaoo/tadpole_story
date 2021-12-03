package xh.zero.tadpolestory.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xh.zero.tadpolestory.databinding.FragmentMoreSubscribeBinding
import xh.zero.tadpolestory.ui.BaseFragment

class MoreSubscribeFragment : BaseFragment<FragmentMoreSubscribeBinding>() {

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