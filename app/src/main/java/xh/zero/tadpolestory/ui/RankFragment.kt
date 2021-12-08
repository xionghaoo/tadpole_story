package xh.zero.tadpolestory.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentRankBinding

class RankFragment : BaseFragment<FragmentRankBinding>() {

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentRankBinding {
        return FragmentRankBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}