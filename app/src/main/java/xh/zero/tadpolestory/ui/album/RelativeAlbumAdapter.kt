package xh.zero.tadpolestory.ui.album

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.data.Album
import kotlin.math.roundToInt

class RelativeAlbumAdapter(
    items: List<Album>,
    private val onItemClick: (Album) -> Unit
) : PlainListAdapter<Album>(items) {
    override fun bindView(v: View, item: Album, position: Int) {
        v.findViewById<TextView>(R.id.tv_album_title).text = item.album_title
        v.findViewById<TextView>(R.id.tv_album_desc).text = item.album_intro
        Glide.with(v.context)
            .load(item.cover_url_large)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(v.context.resources.getDimension(R.dimen._18dp).roundToInt())))
            .into(v.findViewById<ImageView>(R.id.iv_album_icon))

        v.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun itemLayoutId(): Int = R.layout.list_item_relative_album
}