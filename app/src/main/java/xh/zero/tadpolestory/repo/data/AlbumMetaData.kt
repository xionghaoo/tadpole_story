package xh.zero.tadpolestory.repo.data

class AlbumMetaData {

    /**
     * kind : metadata
     * attributes : [{"attr_key":19,"display_name":"故事","attr_value":"故事"},{"attr_key":19,"display_name":"儿歌","attr_value":"儿歌"},{"attr_key":19,"display_name":"早教","attr_value":"儿童成长"},{"attr_key":19,"display_name":"绘本","attr_value":"绘本"},{"attr_key":19,"display_name":"英语","attr_value":"英语"},{"attr_key":19,"display_name":"家教","attr_value":"家长升级"},{"attr_key":19,"display_name":"科普","attr_value":"科普"},{"attr_key":19,"display_name":"国学历史","attr_value":"国学历史"},{"attr_key":19,"display_name":"卡通","attr_value":"卡通"},{"attr_key":19,"display_name":"宝贝show","attr_value":"宝贝show"},{"attr_key":19,"display_name":"胎教","attr_value":"胎教"},{"attr_key":19,"display_name":"健康","attr_value":"健康"},{"attr_key":19,"display_name":"护理","attr_value":"护理"},{"attr_key":19,"display_name":"心理","attr_value":"心理"},{"attr_key":19,"display_name":"教材","attr_value":"教材"},{"attr_key":19,"display_name":"文学","attr_value":"文学"},{"attr_key":19,"display_name":"诗词","attr_value":"诗词"}]
     * display_name : 全部内容
     */
    var kind: String? = null
    var display_name: String? = null

    /**
     * attr_key : 19
     * display_name : 故事
     * attr_value : 故事
     */
    var attributes: List<Attributes>? = null

    class Attributes {
        var attr_key: Int = 0
        var display_name: String? = null
        var attr_value: String? = null

        /**
         * kind : metadata
         * attributes : [{"attr_key":160,"display_name":"童话神话","attr_value":"童话神话"},{"attr_key":160,"display_name":"小说名著","attr_value":"小说名著"},{"attr_key":160,"display_name":"诗歌散文","attr_value":"诗歌散文"},{"attr_key":160,"display_name":"传记","attr_value":"传记"},{"attr_key":160,"display_name":"绘本","attr_value":"绘本"}]
         * display_name : 全部儿童文学
         */
        var child_metadatas: List<ChildMetadatas>? = null
    }

    class ChildMetadatas {
        var kind: String? = null
        var display_name: String? = null

        /**
         * attr_key : 160
         * display_name : 童话神话
         * attr_value : 童话神话
         */
        var attributes: List<Attributes>? = null

        class Attributes {
            var attr_key = 0
            var display_name: String? = null
            var attr_value: String? = null
        }
    }
}