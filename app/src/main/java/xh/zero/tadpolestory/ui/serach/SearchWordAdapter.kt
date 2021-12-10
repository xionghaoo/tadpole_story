package xh.zero.tadpolestory.ui.serach

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.data.SearchWordResult

class SearchWordAdapter(
    items: List<SearchWordResult.Keywords> = emptyList(),
    private val onItemClick: (String?) -> Unit
) : PlainListAdapter<SearchWordResult.Keywords>(items) {

    var currentSearchWord: String = ""

    override fun bindView(v: View, item: SearchWordResult.Keywords, position: Int) {
        val ss = SpannableString(item.keyword)
        val start = item.keyword?.indexOf(currentSearchWord)
        if (start != null) {
            ss.setSpan(
                ForegroundColorSpan(v.resources.getColor(R.color.color_FF9F00)),
                start,
                start + currentSearchWord.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        v.findViewById<TextView>(R.id.tv_search_word_title).text = ss

        v.setOnClickListener {
            onItemClick(item.keyword)
        }
    }

    override fun itemLayoutId(): Int = R.layout.list_item_search_words
}