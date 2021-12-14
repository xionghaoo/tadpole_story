package xh.zero.tadpolestory.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentDayRecommendBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.ui.BaseFragment

@AndroidEntryPoint
class DayRecommendFragment : BaseFragment<FragmentDayRecommendBinding>() {

    private val viewModel: DayRecommendViewModel by viewModels()

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentDayRecommendBinding {
        return FragmentDayRecommendBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

//        viewModel.uploadPlayRecords().observe(this) {
//            handleResponse(it) { r ->
//
//            }
//        }
    }
}