package xh.zero.tadpolestory.test;

import java.util.List;

public class Test {


    /**
     * albums : []
     * keywords : [{"highlight_keyword":"<em>可<\/em>转债","id":16006590,"keyword":"可转债"},{"highlight_keyword":"<em>可<\/em>乐","id":15996322,"keyword":"可乐"}]
     * album_total_count : 0
     * keyword_total_count : 2
     */

    public int album_total_count;
    public int keyword_total_count;
    public List<Object> albums;
    /**
     * highlight_keyword : <em>可</em>转债
     * id : 16006590
     * keyword : 可转债
     */

    public List<Keywords> keywords;

    public static class Keywords {
        public String highlight_keyword;
        public int id;
        public String keyword;
    }
}
