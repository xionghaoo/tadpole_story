package xh.zero.tadpolestory.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import xh.zero.tadpolestory.GlideApp
import xh.zero.tadpolestory.R
import kotlin.math.roundToInt

class TadpoleUtil {
    companion object {
        fun loadAvatar(context: Context?, iv: ImageView, url: String, radius: Int? = null) {
            if (context == null) return
            Glide.with(context)
                .load(url)
                .apply(
                    RequestOptions.bitmapTransform(
                        RoundedCorners(radius ?: context.resources.getDimension(R.dimen._24dp).roundToInt())
                    ))
                .into(iv)
        }
    }
}