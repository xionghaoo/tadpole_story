package xh.zero.tadpolestory.repo.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xh.zero.tadpolestory.repo.tables.SearchHistory

@Dao
abstract class SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(item: SearchHistory)

    @Query("SELECT * FROM SearchHistory")
    abstract fun findAll() : LiveData<List<SearchHistory>>

    @Query("DELETE FROM SearchHistory")
    abstract suspend fun clear()
}