package xh.zero.tadpolestory.ui.home

import android.view.View
import android.widget.TextView
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.utils.TadpoleUtil

class GuessLikeAdapter(
    items: List<Album> = emptyList(),
    private val onItemClick: (Album) -> Unit
) : PlainListAdapter<Album>(items) {
    override fun bindView(v: View, item: Album, position: Int) {
        item.apply {
            // 偶数位item
            v.findViewById<TextView>(R.id.tv_album_title).text = album_title
            v.findViewById<TextView>(R.id.tv_album_desc).text = album_tags
            v.findViewById<TextView>(R.id.tv_album_subscribe).text = "$subscribe_count"
            v.findViewById<TextView>(R.id.tv_album_total).text = "${include_track_count}集"

            TadpoleUtil.loadAvatar(v.context, v.findViewById(R.id.iv_album_cover), item.cover_url_middle.orEmpty())

            v.setOnClickListener { onItemClick(this) }
        }
    }

    override fun itemLayoutId(): Int = R.layout.list_item_filter

}