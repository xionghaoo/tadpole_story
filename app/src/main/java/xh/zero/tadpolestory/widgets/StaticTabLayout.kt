package xh.zero.tadpolestory.widgets

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.viewpager2.widget.ViewPager2
import xh.zero.tadpolestory.R

class StaticTabLayout : LinearLayout {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        orientation = HORIZONTAL
    }

    fun setViewPager(vp: ViewPager2, titles: Array<String>) {
        vp.isUserInputEnabled = false
        removeAllViews()
        titles.forEachIndexed { index, title ->
            val tab = LayoutInflater.from(context).inflate(R.layout.tab_item_view, null)
            tab.findViewById<TextView>(R.id.tv_tab_title).text = title
            tab.setPadding(
                resources.getDimension(R.dimen._17dp).toInt(),
                resources.getDimension(R.dimen._8dp).toInt(),
                resources.getDimension(R.dimen._17dp).toInt(),
                resources.getDimension(R.dimen._5dp).toInt(),
            )
            addView(tab)
            val lp = tab.layoutParams as LinearLayout.LayoutParams
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            if (index > 0) {
                lp.leftMargin = resources.getDimension(R.dimen._24dp).toInt()
            } else {
                selectTab(index)
            }
            tab.setOnClickListener {
                selectTab(index)
                vp.setCurrentItem(index, false)
            }
        }

    }

    private fun selectTab(index: Int) {
        children.forEachIndexed { pos, v ->
            val tvTitle = v.findViewById<TextView>(R.id.tv_tab_title)
            val vIndicator = v.findViewById<View>(R.id.v_tab_indicator)

            if (index == pos) {
                tvTitle.setTextColor(resources.getColor(R.color.color_FF9F00))
                tvTitle.setTypeface(null, Typeface.BOLD)
                vIndicator.visibility = VISIBLE
            } else {
                tvTitle.setTextColor(resources.getColor(R.color.color_161723))
                tvTitle.setTypeface(null, Typeface.NORMAL)
                vIndicator.visibility = INVISIBLE
            }
        }
    }
}