package xh.zero.tadpolestory.repo.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SearchHistory")
class SearchHistory {
    @PrimaryKey
    var keyword: String = ""

    var created: Long = 0
}