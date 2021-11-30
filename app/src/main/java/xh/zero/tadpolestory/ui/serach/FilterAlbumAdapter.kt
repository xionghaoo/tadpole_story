package xh.zero.tadpolestory.ui.serach

import android.view.View
import android.widget.TextView
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.data.Album

class FilterAlbumAdapter(
    private val items: List<Album> = emptyList()
) : PlainListAdapter<Album>(items) {
    override fun bindView(v: View, item: Album, position: Int) {
//        v.findViewById<TextView>(R.id.)
    }

    override fun itemLayoutId(): Int = R.layout.list_item_filter
}