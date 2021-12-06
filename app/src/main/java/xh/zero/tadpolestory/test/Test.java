package xh.zero.tadpolestory.test;

import java.util.List;

public class Test {

    /**
     * attr_key : 8
     * display_name : 儿童文学
     * child_metadatas : [{"kind":"metadata","attributes":[{"attr_key":160,"display_name":"童话神话","attr_value":"童话神话"},{"attr_key":160,"display_name":"小说名著","attr_value":"小说名著"},{"attr_key":160,"display_name":"诗歌散文","attr_value":"诗歌散文"},{"attr_key":160,"display_name":"传记","attr_value":"传记"},{"attr_key":160,"display_name":"绘本","attr_value":"绘本"}],"display_name":"全部儿童文学"}]
     * attr_value : 儿童文学
     */

    public int attr_key;
    public String display_name;
    public String attr_value;
    /**
     * kind : metadata
     * attributes : [{"attr_key":160,"display_name":"童话神话","attr_value":"童话神话"},{"attr_key":160,"display_name":"小说名著","attr_value":"小说名著"},{"attr_key":160,"display_name":"诗歌散文","attr_value":"诗歌散文"},{"attr_key":160,"display_name":"传记","attr_value":"传记"},{"attr_key":160,"display_name":"绘本","attr_value":"绘本"}]
     * display_name : 全部儿童文学
     */

    public List<ChildMetadatas> child_metadatas;

    public static class ChildMetadatas {
        public String kind;
        public String display_name;
        /**
         * attr_key : 160
         * display_name : 童话神话
         * attr_value : 童话神话
         */

        public List<Attributes> attributes;

        public static class Attributes {
            public int attr_key;
            public String display_name;
            public String attr_value;
        }
    }
}
