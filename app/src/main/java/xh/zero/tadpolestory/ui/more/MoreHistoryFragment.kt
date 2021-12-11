package xh.zero.tadpolestory.ui.more

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentMoreHistoryBinding
import xh.zero.tadpolestory.ui.BaseFragment

@AndroidEntryPoint
class MoreHistoryFragment : BaseFragment<FragmentMoreHistoryBinding>() {

    private val viewModel: MoreViewModel by viewModels()

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMoreHistoryBinding {
        return FragmentMoreHistoryBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.repo.loadAllAlbums().observe(viewLifecycleOwner) { albums ->
            Timber.d("local albums: ${albums.size}")
        }
    }

    companion object {

        fun newInstance() = MoreHistoryFragment()
    }
}