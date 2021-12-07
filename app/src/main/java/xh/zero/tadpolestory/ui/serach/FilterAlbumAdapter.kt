package xh.zero.tadpolestory.ui.serach

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.core.paging.PagingAdapter
import xh.zero.core.vo.NetworkState
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.ui.TadpoleNetworkStateViewHolder
import kotlin.math.roundToInt

class FilterAlbumAdapter(
    private val onItemClick: (Album) -> Unit,
    private val retry: () -> Unit,

) : PagingAdapter<Album>(Album.DIFF, retry) {

    override fun bindItemView(v: View, item: Album?, position: Int) {
        if (item == null) return
        item.extraAlbum?.apply {
            // 偶数位item
            v.findViewById<TextView>(R.id.tv_album_title).text = album_title
            v.findViewById<TextView>(R.id.tv_album_desc).text = album_tags
            v.findViewById<TextView>(R.id.tv_album_subscribe).text = "$subscribe_count"
            v.findViewById<TextView>(R.id.tv_album_total).text = "${include_track_count}集"

            Glide.with(v.context)
                .load(cover_url_large)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(v.context.resources.getDimension(R.dimen._24dp).roundToInt())))
                .into(v.findViewById<ImageView>(R.id.iv_album_cover))

            v.setOnClickListener { onItemClick(this) }
        }
        // 奇数位item
        bindItem2View(v, item)
    }

    private fun bindItem2View(v: View, item: Album?) {
        if (item == null) return
        v.findViewById<TextView>(R.id.tv_album_title_2).text = item.album_title
        v.findViewById<TextView>(R.id.tv_album_desc_2).text = item.album_tags
        v.findViewById<TextView>(R.id.tv_album_subscribe_2).text = "${item.subscribe_count}"
        v.findViewById<TextView>(R.id.tv_album_total_2).text = "${item.include_track_count}集"

        Glide.with(v.context)
            .load(item.cover_url_large)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(v.context.resources.getDimension(R.dimen._24dp).roundToInt())))
            .into(v.findViewById<ImageView>(R.id.iv_album_cover_2))

        v.setOnClickListener { onItemClick(item) }
    }

    override fun itemLayout(): Int = R.layout.list_item_filter_albums

    override fun networkStateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return TadpoleNetworkStateViewHolder.create(parent, retry)
    }

    override fun onBindNetworkStateView(holder: RecyclerView.ViewHolder, state: NetworkState?) {
        (holder as TadpoleNetworkStateViewHolder).bindTo(state)
    }

}