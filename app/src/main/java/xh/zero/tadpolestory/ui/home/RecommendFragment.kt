package xh.zero.tadpolestory.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.databinding.FragmentDayRecommendBinding
import xh.zero.tadpolestory.ui.BaseFragment

@AndroidEntryPoint
class RecommendFragment : BaseFragment<FragmentDayRecommendBinding>() {

    private val viewModel: RecommendViewModel by viewModels()
    private lateinit var adapter: RecommendAlbumAdapter
    private val args: RecommendFragmentArgs by navArgs()

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

        adapter = RecommendAlbumAdapter(
            onItemClick = { album ->
                findNavController().navigate(RecommendFragmentDirections.actionDayRecommendFragmentToAlbumDetailFragment(album))
            },
            retry = {
                viewModel.retry()
            }
        )
        binding.rcAlbumList.layoutManager = LinearLayoutManager(context)
        binding.rcAlbumList.isSaveEnabled = true
        binding.rcAlbumList.adapter = adapter
        viewModel.itemList.observe(this) {
            adapter.submitList(it)
        }
        viewModel.networkState.observe(this) {
            adapter.setNetworkState(it)
        }
        viewModel.refreshState.observe(this) {

        }

        viewModel.showList(listOf(args.categoryId.toString()))

    }
}