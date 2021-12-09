package xh.zero.tadpolestory.repo

import androidx.room.Database
import androidx.room.RoomDatabase
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.repo.dao.SearchHistoryDao
import xh.zero.tadpolestory.repo.tables.SearchHistory

@Database(
    entities = [SearchHistory::class],
    version = Configs.DB_VERSION,
    exportSchema = false
)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun searchHistoryDao() : SearchHistoryDao
}