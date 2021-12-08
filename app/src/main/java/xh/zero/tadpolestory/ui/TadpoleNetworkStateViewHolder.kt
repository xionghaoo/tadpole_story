package xh.zero.tadpolestory.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xh.zero.core.vo.NetworkState
import xh.zero.core.vo.Status
import xh.zero.tadpolestory.R

class TadpoleNetworkStateViewHolder(
    view: View,
    private val retryCallback: () -> Unit
) : RecyclerView.ViewHolder(view) {
    private val progressBar = view.findViewById<View>(R.id.progress_bar)
    private val retry = view.findViewById<Button>(R.id.retry_button)
    private val errorMsg = view.findViewById<TextView>(R.id.error_msg)
    private val noMoreData = view.findViewById<TextView>(R.id.no_more_data)

    init {
        retry.setOnClickListener {
            retryCallback()
        }
    }

    // TODO 加载失败时重试
    fun bindTo(networkState: NetworkState?, isNoMoreData: Boolean = true) {
        progressBar.visibility = toVisibility(networkState?.status == Status.LOADING)
//        retry.visibility = toVisibility(networkState?.status == Status.ERROR)
        errorMsg.visibility = toVisibility(networkState?.msg != null)
        errorMsg.text = "加载失败"
        noMoreData.visibility = toVisibility(networkState?.status == Status.SUCCESS && isNoMoreData)
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): TadpoleNetworkStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.network_state_item_tadpole, parent, false)
            return TadpoleNetworkStateViewHolder(view, retryCallback)
        }

        fun toVisibility(constraint : Boolean): Int {
            return if (constraint) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}