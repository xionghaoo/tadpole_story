package xh.zero.tadpolestory.ui.serach

import android.view.View
import android.widget.TextView
import xh.zero.core.adapter.PlainListAdapter
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.repo.data.SearchWordResult

class SearchWordAdapter(
    items: List<SearchWordResult.Keywords> = emptyList(),
    private val onItemClick: (String?) -> Unit
) : PlainListAdapter<SearchWordResult.Keywords>(items) {

    override fun bindView(v: View, item: SearchWordResult.Keywords, position: Int) {
        v.findViewById<TextView>(R.id.tv_search_word_title).text = item.keyword

//        v.findViewById<TextView>(R.id.tv_search_word_count).text = ""
        v.setOnClickListener {
            onItemClick(item.keyword)
        }
    }

    override fun itemLayoutId(): Int = R.layout.list_item_search_words
}