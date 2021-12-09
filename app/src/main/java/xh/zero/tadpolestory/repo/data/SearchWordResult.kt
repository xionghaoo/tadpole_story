package xh.zero.tadpolestory.repo.data

class SearchWordResult {
    /**
     * albums : []
     * keywords : [{"highlight_keyword":"*可<\/em>转债","id":16006590,"keyword":"可转债"},{"highlight_keyword":"*可<\/em>乐","id":15996322,"keyword":"可乐"}]
     * album_total_count : 0
     * keyword_total_count : 2
     ** */
    var album_total_count = 0
    var keyword_total_count = 0
    var albums: List<Any>? = null

    /**
     * highlight_keyword : *可*转债
     * id : 16006590
     * keyword : 可转债
     */
    var keywords: List<Keywords>? = null

    class Keywords {
        var highlight_keyword: String? = null
        var id = 0
        var keyword: String? = null
    }
}