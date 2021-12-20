package xh.zero.tadpolestory.ui.album

import android.view.View
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import timber.log.Timber
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.ui.MediaItemData
import xh.zero.tadpolestory.utils.TimeUtil

class NowPlayingTrackAdapter(
    private val items: List<MediaItemData> = emptyList(),
    private val onItemClick: (Int) -> Unit
) : PlainListAdapter<MediaItemData>(items) {

    private var nowPlayingMediaId: String? = null
    private var isPlaying: Boolean? = null

    override fun bindView(v: View, item: MediaItemData, position: Int) {
        item.also { data ->
            val tvTrackIndex = v.findViewById<TextView>(R.id.tv_track_index)
            val tvTrackTitle = v.findViewById<TextView>(R.id.tv_track_title)
            val tvTrackTime = v.findViewById<TextView>(R.id.tv_track_time_count)
            tvTrackIndex.text = "${data.trackNumber + 1}"
            tvTrackTitle.text = data.title
            tvTrackTime.text = TimeUtil.secondsFormat(data.duration)
            v.setOnClickListener {
                onItemClick(position)
            }

            val animView = v.findViewById<LottieAnimationView>(R.id.anim_playing)
            // 显示正在播放的状态
            if (item.mediaId == nowPlayingMediaId) {
                animView.visibility = View.VISIBLE
                if (isPlaying == true) {
                    animView.playAnimation()
                } else {
                    animView.cancelAnimation()
                }
            } else {
                animView.cancelAnimation()
                animView.visibility = View.GONE
            }

            // 高亮
            if (item.mediaId == nowPlayingMediaId) {
                val color = v.context.resources.getColor(R.color.color_FF9F00)
                tvTrackIndex.setTextColor(color)
                tvTrackTitle.setTextColor(color)
            } else {
                val color = v.context.resources.getColor(R.color.color_0A1A2A)
                tvTrackIndex.setTextColor(color)
                tvTrackTitle.setTextColor(color)
            }
        }
    }

    override fun itemLayoutId(): Int = R.layout.list_item_track

    /**
     * 更新当前正在播放的音轨
     * TODO 待优化
     */
    fun updateNowPlayingItem(item: MediaItemData) {
        nowPlayingMediaId = item.mediaId
        isPlaying = item.isPlaying
//        var position = 0
//        items.forEachIndexed { index, data ->
//            if (data.mediaId == item.mediaId) {
//                position = index
//                return@forEachIndexed
//            }
//        }
////        notifyItemChanged(nowPlayingIndex)
//        notifyItemChanged(position)
        notifyDataSetChanged()
        Timber.d("updateNowPlayingItem: ${item.title}, ${item.mediaId}, ${item.trackNumber}")
    }
}