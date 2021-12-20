package xh.zero.tadpolestory.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.WidgetSubscribeButtonBinding

class SubscribeButton : LinearLayoutCompat {

    private var binding: WidgetSubscribeButtonBinding =
        WidgetSubscribeButtonBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }

    fun setSubscribe(isSubscribe: Boolean, onSubscribeCall: () -> Unit, onUnsubscribeCall: () -> Unit) {
        if (isSubscribe) {
            binding.tvSubscribeTitle.text = "已订阅"
            binding.tvSubscribeTitle.setTextColor(resources.getColor(R.color.color_6F6F72))
            binding.ivSubscribeIcon.setImageResource(R.mipmap.ic_subscribed)
            binding.root.setBackgroundResource(R.drawable.shape_btn_random)
            binding.root.setOnClickListener {
//                unsubscribe()
                onUnsubscribeCall()
            }
        } else {
            binding.tvSubscribeTitle.text = "订阅"
            binding.tvSubscribeTitle.setTextColor(Color.WHITE)
            binding.ivSubscribeIcon.setImageResource(R.mipmap.ic_subscribe_30)
            binding.root.setBackgroundResource(R.drawable.shape_btn_subscribe)
            binding.root.setOnClickListener {
//                subscribe()
                onSubscribeCall()
            }
        }
    }
}
