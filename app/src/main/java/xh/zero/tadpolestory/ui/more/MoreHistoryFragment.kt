package xh.zero.tadpolestory.ui.more

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.utils.DateUtil
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentMoreHistoryBinding
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.ui.MainFragmentDirections
import xh.zero.tadpolestory.utils.TadpoleUtil
import java.util.*
import kotlin.collections.ArrayList

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
        viewModel.repo.loadAllAlbums().observe(this) { albums ->
            val todayAlbums = ArrayList<Album>()
            val moreAlbums = ArrayList<Album>()
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)
            Timber.d("today: ${cal}")
            cal.add(Calendar.DATE, 0)
            albums.forEachIndexed { index, album ->
                if (album.createdTime >= cal.time.time) {
                    todayAlbums.add(album)
                } else {
                    moreAlbums.add(album)
                }
            }

            if (todayAlbums.isNotEmpty()) {
                binding.tvTodayTitle.text = "今天"
                binding.flTodayList.removeAllViews()
                createHistoryView(binding.flTodayList, todayAlbums)
            } else {
                binding.vFirst.visibility = View.GONE
            }

            if (moreAlbums.isNotEmpty()) {
                binding.tvMoreTitle.text = "更早"
                binding.flMoreList.removeAllViews()
                createHistoryView(binding.flMoreList, moreAlbums)
            } else {
                binding.vSecond.visibility = View.GONE
            }

        }
    }

    private fun createHistoryView(container: ViewGroup, albums: List<Album>) {
        container.removeAllViews()
        albums.forEach { album ->
            val itemView = layoutInflater.inflate(R.layout.list_item_history_album, null)
            container.addView(itemView)
            val lp = itemView.layoutParams as FlexboxLayout.LayoutParams
            lp.width = resources.getDimension(R.dimen._412dp).toInt()
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.rightMargin = resources.getDimension(R.dimen._40dp).toInt()
            lp.bottomMargin = resources.getDimension(R.dimen._16dp).toInt()
            lp.topMargin = resources.getDimension(R.dimen._16dp).toInt()
            bindItemView(itemView, album)
        }
    }

    private fun bindItemView(v: View, item: Album) {
        item.apply {
            // 偶数位item
            v.findViewById<TextView>(R.id.tv_album_title).text = album_title
            v.findViewById<TextView>(R.id.tv_album_desc).text = album_tags
            v.findViewById<TextView>(R.id.tv_album_subscribe).text = "$subscribe_count"
            v.findViewById<TextView>(R.id.tv_album_total).text = "${include_track_count}集"

            TadpoleUtil.loadAvatar(v.context, v.findViewById(R.id.iv_album_cover), item.cover_url_middle.orEmpty())
            v.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToAlbumDetailFragment(item))
            }
        }
    }

    companion object {

        fun newInstance() = MoreHistoryFragment()
    }
}