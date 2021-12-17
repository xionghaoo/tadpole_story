package xh.zero.tadpolestory.ui.album

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import timber.log.Timber
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.core.vo.NetworkState
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.PreferenceStorage
import xh.zero.tadpolestory.ui.MediaItemData
import xh.zero.tadpolestory.ui.TadpoleNetworkStateViewHolder
import xh.zero.tadpolestory.utils.TimeUtil

class TrackAdapter(
    private val totalCount: Int,
    private val prefs: PreferenceStorage,
    private val onItemClick: (MediaItemData) -> Unit,
    private val retry: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var networkState: NetworkState? = null
    private var nowPlayingMediaId: String? = null
    private var isPlaying: Boolean? = null

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v)

    private val mDiffer: AsyncListDiffer<MediaItemData> =
        AsyncListDiffer(this, MediaItemData.DIFF)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            TYPE_CONTENT -> {
                return ViewHolder(inflater.inflate(R.layout.list_item_tracks, parent, false))
            }
            TYPE_LOAD_MORE -> {
                return TadpoleNetworkStateViewHolder.create(parent, retry)
            }
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_CONTENT -> {
                bindView(holder.itemView, mDiffer.currentList[position], position)
            }
            TYPE_LOAD_MORE -> {
                val itemLines = if (totalCount % 2 == 0) totalCount / 2 else totalCount / 2 + 1
                (holder as TadpoleNetworkStateViewHolder).bindTo(networkState, /*itemCount >= itemLines + 1*/)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == itemCount - 1) {
            return TYPE_LOAD_MORE
        } else {
            return TYPE_CONTENT
        }
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size + if (hasExtraRow()) 1 else 0
    }

    fun submitList(list: List<MediaItemData>?) {
        mDiffer.submitList(list)
    }

    private fun hasExtraRow() = networkState != null

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()

        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
//            notifyItemChanged(itemCount - 1)  // 这里会导致recycler view自动滚动到最后一项
            notifyDataSetChanged()

//            if (hadExtraRow) {
//                // 这里指从Loading或Error变为Success
//                notifyItemRemoved(super.getItemCount())
//            } else {
//                // 这里表示从Success变为Loading或Error
//                notifyItemInserted(super.getItemCount())
//            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyDataSetChanged()

            // 这里指Loading和ERROR的状态，如果两个状态互相切换，通知尾项的变化
//            notifyItemChanged(itemCount - 1)
        }
    }

    private fun bindView(v: View, item: MediaItemData, position: Int) {
        item.also { data ->
            v.findViewById<TextView>(R.id.tv_track_index).text = "${data.trackNumber + 1}"
            v.findViewById<TextView>(R.id.tv_track_title).text = data.title
            v.findViewById<TextView>(R.id.tv_track_time_count).text = TimeUtil.secondsFormat(data.duration)
            v.findViewById<View>(R.id.v_first).setOnClickListener {
                onItemClick(data)
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
        }

        val extraContainer = v.findViewById<View>(R.id.v_second)
        extraContainer.visibility = if (item.extraItem == null) View.GONE else View.VISIBLE
        item.extraItem?.also { data ->
            v.findViewById<TextView>(R.id.tv_track_index_2).text = "${data.trackNumber + 1}"
            v.findViewById<TextView>(R.id.tv_track_title_2).text = data.title
            v.findViewById<TextView>(R.id.tv_track_time_count_2).text = TimeUtil.secondsFormat(data.duration)
            extraContainer.setOnClickListener {
                onItemClick(data)
            }

            val animView = v.findViewById<LottieAnimationView>(R.id.anim_playing_2)
            // 显示正在播放的状态
            if (data.mediaId == nowPlayingMediaId) {
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
        }

    }

    /**
     * 更新当前正在播放的音轨
     * TODO 待优化
     */
    fun updateNowPlayingItem(item: MediaItemData) {
//        var lastPlayingItem: MediaItemData? = null
//        var lastPlayingItemIndex: Int = 0
//        mDiffer.currentList.forEachIndexed { index, mediaItemData ->
//            if (mediaItemData.playbackRes == 1) {
//                lastPlayingItem = mediaItemData
//                return@forEachIndexed
//            }
//        }
//        if (item.mediaId == lastPlayingItem?.mediaId) {
//            return
//        }
//        lastPlayingItem?.playbackRes = 0
//        notifyItemChanged(lastPlayingItemIndex)
//        var nowPlayingItem: MediaItemData? = null
//        var nowPlayingItemIndex: Int = 0
//        mDiffer.currentList.forEachIndexed { index, mediaItemData ->
//            if (mediaItemData.mediaId == item.mediaId) {
//                nowPlayingItem = mediaItemData
//                return@forEachIndexed
//            }
//        }
//        nowPlayingItem?.playbackRes = 1
//        notifyItemChanged(nowPlayingItemIndex)
        nowPlayingMediaId = item.mediaId
        isPlaying = item.isPlaying
        notifyDataSetChanged()
        Timber.d("updateNowPlayingItem: ${item.title}, ${item.mediaId}, ${item.trackNumber}")
    }

    companion object {
        private const val TYPE_CONTENT = 0
        private const val TYPE_LOAD_MORE = 1
    }
}