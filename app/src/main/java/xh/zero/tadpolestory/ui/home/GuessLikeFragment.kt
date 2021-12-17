package xh.zero.tadpolestory.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentGuessLikeBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.ui.MainFragmentDirections

/**
 * 猜你喜欢
 */
@AndroidEntryPoint
class GuessLikeFragment : BaseFragment<FragmentGuessLikeBinding>() {

    private val viewModel: RecommendViewModel by viewModels()
    private lateinit var adapter: GuessLikeAdapter

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentGuessLikeBinding {
        return FragmentGuessLikeBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.rcAlbumList.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = GuessLikeAdapter { item ->
            findNavController().navigate(GuessLikeFragmentDirections.actionGuessLikeFragmentToAlbumDetailFragment(item))
        }
        binding.rcAlbumList.adapter = adapter
        binding.rcAlbumList.isSaveEnabled = true
        loadData()
    }

    private fun loadData() {
        viewModel.getGuessLikeAlbums().observe(this) {
            handleResponse(it) { r ->
                adapter.updateData(r)
            }
        }
    }
}