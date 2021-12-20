package xh.zero.tadpolestory.ui.rank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentRankBinding
import xh.zero.tadpolestory.ui.BaseFragment

@AndroidEntryPoint
class RankFragment : BaseFragment<FragmentRankBinding>() {

    private val args: RankFragmentArgs by navArgs()
    private val viewModel: RankViewModel by viewModels()
    private lateinit var adapter: RankAdapter

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

        binding.btnRankHot.setOnClickListener {

            binding.btnRankHotTitle.setTextColor(resources.getColor(R.color.white))
            binding.btnRankNewTitle.setTextColor(resources.getColor(R.color.color_6F6F72))
            binding.btnRankEvaluateTitle.setTextColor(resources.getColor(R.color.color_6F6F72))

            binding.btnRankHot.setBackgroundResource(R.drawable.shape_album_tag_selected)
            binding.btnRankNew.setBackgroundResource(R.drawable.shape_rank_tag)
            binding.btnRankEvaluate.setBackgroundResource(R.drawable.shape_rank_tag)

            binding.btnRankHotIcon.setImageResource(R.mipmap.ic_rank_top_hot_selected)
            binding.btnRankNewIcon.setImageResource(R.mipmap.ic_rank_top_new_normal)
            binding.btnRankEvaluateIcon.setImageResource(R.mipmap.ic_rank_top_evaluate_normal)

            loadData(1)
        }
        binding.btnRankNew.setOnClickListener {

            binding.btnRankHotTitle.setTextColor(resources.getColor(R.color.color_6F6F72))
            binding.btnRankNewTitle.setTextColor(resources.getColor(R.color.white))
            binding.btnRankEvaluateTitle.setTextColor(resources.getColor(R.color.color_6F6F72))

            binding.btnRankHot.setBackgroundResource(R.drawable.shape_rank_tag)
            binding.btnRankNew.setBackgroundResource(R.drawable.shape_album_tag_selected)
            binding.btnRankEvaluate.setBackgroundResource(R.drawable.shape_rank_tag)

            binding.btnRankHotIcon.setImageResource(R.mipmap.ic_rank_top_hot_normal)
            binding.btnRankNewIcon.setImageResource(R.mipmap.ic_rank_top_new_selected)
            binding.btnRankEvaluateIcon.setImageResource(R.mipmap.ic_rank_top_evaluate_normal)

            loadData(2)
        }
        binding.btnRankEvaluate.setOnClickListener {
            binding.btnRankHotTitle.setTextColor(resources.getColor(R.color.color_6F6F72))
            binding.btnRankNewTitle.setTextColor(resources.getColor(R.color.color_6F6F72))
            binding.btnRankEvaluateTitle.setTextColor(resources.getColor(R.color.white))

            binding.btnRankHot.setBackgroundResource(R.drawable.shape_rank_tag)
            binding.btnRankNew.setBackgroundResource(R.drawable.shape_rank_tag)
            binding.btnRankEvaluate.setBackgroundResource(R.drawable.shape_album_tag_selected)

            binding.btnRankHotIcon.setImageResource(R.mipmap.ic_rank_top_hot_normal)
            binding.btnRankNewIcon.setImageResource(R.mipmap.ic_rank_top_new_normal)
            binding.btnRankEvaluateIcon.setImageResource(R.mipmap.ic_rank_top_evaluate_selected)

            loadData(3)
        }

        binding.rcAlbumList.layoutManager = LinearLayoutManager(context)
        binding.rcAlbumList.isSaveEnabled = true
        adapter = RankAdapter(
            onItemClick = { album ->
                findNavController().navigate(RankFragmentDirections.actionRankFragmentToAlbumDetailFragment(album))
            },
            retry = viewModel::retry
        )
        binding.rcAlbumList.adapter = adapter

        viewModel.itemList.observe(this) {
            adapter.submitList(it)
        }
        viewModel.networkState.observe(this) {
            adapter.setNetworkState(it)
        }

        loadData(1)

    }

    private fun loadData(calDimen: Int) {
        viewModel.showList(listOf(calDimen.toString(), args.categoryId.toString()))
    }
}