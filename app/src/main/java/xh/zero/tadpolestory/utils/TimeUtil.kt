package xh.zero.tadpolestory.utils

import android.content.Context

class TimeUtil {
    companion object {
//        fun timestampToMSS(context: Context, position: Long): String {
//            val totalSeconds = Math.floor(position / 1E3).toInt()
//            val minutes = totalSeconds / 60
//            val remainingSeconds = totalSeconds - (minutes * 60)
//            return if (position < 0) context.getString(R.string.duration_unknown)
//            else context.getString(R.string.duration_format).format(minutes, remainingSeconds)
//        }

        fun secondsFormat(millSec: Long) : String {
            val sec = millSec / 1000
            val seconds = sec % 60
            val minutes = sec / 60
            val secondsText = if (seconds < 10) "0${seconds}" else "$seconds"
            return "${minutes}分${secondsText}秒"
        }
    }
}