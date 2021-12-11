package xh.zero.tadpolestory.repo.data

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = "Album")
@Parcelize
data class Album(
    @PrimaryKey
    var id: Int = 0,
    var album_intro: String? = null,
    var category_id: Int = 0,
    var cover_url_large: String? = null,
    var kind: String? = null,
    var include_track_count: Long = 0,
    var meta: String? = null,
    var recommend_reason: String? = null,
    var album_tags: String? = null,
    var subscribe_count: Long = 0,
    var album_title: String? = null,
    var short_rich_intro: String? = null
) : Parcelable {

    /**
     * album_intro : 三字经是宋朝王应麟先生所作，是中国历史最著名的蒙学读物。包括了教育、历史、天文、地理、道德以及一些民间传说，广泛生动而又言简意赅。全文1千多字，三字一句，四句一组，读起来朗朗上口，是孩子学习传统文化的最好入门教材。尽管部分人会认为三字经中有很多传统观念，不符合现在的价值观，但我们认为，孩子在诵读三字经时，应该全文诵读。不管精华或是糟粕，作为传统文化的一部分，都是脱胎于当时的时代土壤，是其自身完整性和丰富性的一部分。为了避免孩子对于传统文化的片面理解，最明智的做法，不是粉碎一个旧世界，而且用现代人的目光去对冲。这里是叫叫老师，了解更多课程请关注微信公众号【叫叫阅读课】
     * announcer : {"anchor_grade":0,"avatar_url":"http://imgopen.xmcdn.com/group48/M0B/AD/6E/wKgKnFuoQbHxs7KhAAC7bMJnWCE929.jpg!op_type=3&columns=110&rows=110","kind":"user","nickname":"叫叫老师","id":124596332,"is_verified":true}
     * cover_url_small : http://imgopen.xmcdn.com/group52/M01/9B/9A/wKgLe1xAU3zzie4LAABcKIw-2WQ321.jpg!op_type=5&upload_type=album&device_type=ios&name=mobile_small&magick=png
     * created_at : 1547719561000
     * recommend_reason : 三字经听读
     * cover_url_middle : http://imgopen.xmcdn.com/group52/M01/9B/9A/wKgLe1xAU3zzie4LAABcKIw-2WQ321.jpg!op_type=5&upload_type=album&device_type=ios&name=medium&magick=png
     * category_id : 92
     * updated_at : 1636972652000
     * favorite_count : 0
     * id : 20691383
     * copyright_source : 1
     * cover_url_large : http://imgopen.xmcdn.com/group52/M01/9B/9A/wKgLe1xAU3zzie4LAABcKIw-2WQ321.jpg!op_type=5&upload_type=album&device_type=ios&name=mobile_large&magick=png
     * cover_url : http://imgopen.xmcdn.com/group52/M01/9B/9A/wKgLe1xAU3zzie4LAABcKIw-2WQ321.jpg
     * wrap_cover_url :
     * homemade : 0
     * kind : album
     * quality_score : 8.3
     * play_count : 20738497
     * include_track_count : 307
     * is_finished : 1
     * share_count : 1
     * meta : 国学,小学,课外读物,语文
     * album_tags : 语文,儿童故事,爆笑,曲小奇,二年级,小学,语文课,小学语文
     * selling_point :
     * subscribe_count : 34183
     * tracks_natural_ordered : true
     * last_uptrack : {"duration":217,"updated_at":1637424000000,"track_id":462571090,"track_title":"口语交际《名字里的故事》","created_at":1637424000000}
     * can_download : true
     * album_title : 曲小奇爆笑上学记（2、3季）|小学生故事
     */


    /**
     * anchor_grade : 0
     * avatar_url : http://imgopen.xmcdn.com/group48/M0B/AD/6E/wKgKnFuoQbHxs7KhAAC7bMJnWCE929.jpg!op_type=3&columns=110&rows=110
     * kind : user
     * nickname : 叫叫老师
     * id : 124596332
     * is_verified : true
     */
//    var announcer: Announcer? = null
    var cover_url_small: String? = null
    var created_at: Long = 0
    var cover_url_middle: String? = null

    var updated_at: Long = 0
    var favorite_count = 0

    var copyright_source = 0

//    var cover_url: String? = null
//    var wrap_cover_url: String? = null
//    var homemade = 0

//    var quality_score: String? = null
    var play_count: Long = 0

//    var is_finished = 0
//    var share_count: Long = 0


//    var selling_point: String? = null

//    var tracks_natural_ordered = false

    @Ignore
    @IgnoredOnParcel
    var tracks: List<Track>? = null

    /**
     * duration : 217.0
     * updated_at : 1637424000000
     * track_id : 462571090
     * track_title : 口语交际《名字里的故事》
     * created_at : 1637424000000
     */
//    var last_uptrack: LastUptrack? = null
//    var can_download = false

    @Ignore
    @IgnoredOnParcel
    var extraAlbum: Album? = null

    /**
     * 分页参数
     */
    @Ignore
    val current_page: Int = 0
    @Ignore
    val total_page: Int = 0

//    class Announcer {
//        var anchor_grade = 0
//        var avatar_url: String? = null
//        var kind: String? = null
//        var nickname: String? = null
//        var id = 0
//        var is_verified = false
//    }
//
//    class LastUptrack {
//        var duration = 0.0
//        var updated_at: Long = 0
//        var track_id = 0
//        var track_title: String? = null
//        var created_at: Long = 0
//    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is Album) {
            return other.id == id
        } else {
            return false
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Album>() {
            override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean =
                oldItem.id == newItem.id
        }
    }
}