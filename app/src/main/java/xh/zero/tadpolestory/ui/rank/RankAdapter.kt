package xh.zero.tadpolestory.ui.rank

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.core.paging.PagingAdapter
import xh.zero.core.vo.NetworkState
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.ui.TadpoleNetworkStateViewHolder
import xh.zero.tadpolestory.utils.TadpoleUtil

class RankAdapter(
    private val onItemClick: (Album) -> Unit,
    private val retry: () -> Unit
) : PagingAdapter<Album>(Album.DIFF, retry) {

    override fun bindItemView(v: View, item: Album?, position: Int) {
        if (item == null) return
        val index = position * 2
        val tvIndex = v.findViewById<TextView>(R.id.tv_rank_index_number)
        val ivIndex = v.findViewById<ImageView>(R.id.iv_rank_index)
        if (index <= 2) {
            tvIndex.visibility = View.GONE
            ivIndex.visibility = View.VISIBLE
            ivIndex.setImageResource(
                when (position * 2) {
                    0 -> R.mipmap.ic_rank_first
                    1 -> R.mipmap.ic_rank_second
                    2 -> R.mipmap.ic_rank_third
                    else -> R.mipmap.ic_rank_third
                }
            )
        } else {
            tvIndex.visibility = View.VISIBLE
            tvIndex.text = index.toString()
            ivIndex.visibility = View.GONE
        }
        TadpoleUtil.loadAvatar(v.context, v.findViewById<ImageView>(R.id.iv_rank_album_cover), item.cover_url_large.orEmpty())
        v.findViewById<TextView>(R.id.tv_rank_album_title).text = item.album_title
        v.findViewById<TextView>(R.id.tv_rank_album_desc).text = item.recommend_reason
        v.findViewById<TextView>(R.id.tv_rank_album_play_count).text = item.play_count.toString()
        v.findViewById<View>(R.id.v_first).setOnClickListener {
            onItemClick(item)
        }

        item.extraAlbum?.also { extraItem ->
            val index2 = position * 2 + 1
            val tvIndex2 = v.findViewById<TextView>(R.id.tv_rank_index_number_2)
            val ivIndex2 = v.findViewById<ImageView>(R.id.iv_rank_index_2)
            if (index2 <= 2) {
                tvIndex2.visibility = View.GONE
                ivIndex2.visibility = View.VISIBLE
                ivIndex2.setImageResource(
                    when (index2) {
                        0 -> R.mipmap.ic_rank_first
                        1 -> R.mipmap.ic_rank_second
                        2 -> R.mipmap.ic_rank_third
                        else -> R.mipmap.ic_rank_third
                    }
                )
            } else {
                ivIndex2.visibility = View.GONE
                tvIndex2.visibility = View.VISIBLE
                tvIndex2.text = index2.toString()
            }
            TadpoleUtil.loadAvatar(v.context, v.findViewById<ImageView>(R.id.iv_rank_album_cover_2), extraItem.cover_url_large.orEmpty())
            v.findViewById<TextView>(R.id.tv_rank_album_title_2).text = extraItem.album_title
            v.findViewById<TextView>(R.id.tv_rank_album_desc_2).text = extraItem.recommend_reason
            v.findViewById<TextView>(R.id.tv_rank_album_play_count_2).text = extraItem.play_count.toString()

            v.findViewById<View>(R.id.v_second).setOnClickListener {
                onItemClick(extraItem)
            }
        }
    }

    override fun itemLayout(): Int = R.layout.list_item_rank_album

    override fun networkStateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return TadpoleNetworkStateViewHolder.create(parent, retry)
    }

    override fun onBindNetworkStateView(holder: RecyclerView.ViewHolder, state: NetworkState?) {
        (holder as TadpoleNetworkStateViewHolder).bindTo(state)
    }
}