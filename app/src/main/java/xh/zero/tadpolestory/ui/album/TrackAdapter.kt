package xh.zero.tadpolestory.ui.album

import android.view.View
import android.widget.TextView
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.ui.MediaItemData

class TrackAdapter(
    private val items: List<MediaItemData>
) : PlainListAdapter<MediaItemData>(items) {
    override fun bindView(v: View, item: MediaItemData, position: Int) {
        v.findViewById<TextView>(R.id.tv_track_index).text = "${position + 1}"
        v.findViewById<TextView>(R.id.tv_track_title).text = item.title
        v.findViewById<TextView>(R.id.tv_track_time_count).text = item.mediaId
    }

    override fun itemLayoutId(): Int = R.layout.list_item_track
}