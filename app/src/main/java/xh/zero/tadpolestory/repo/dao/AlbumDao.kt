package xh.zero.tadpolestory.repo.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.tables.SearchHistory

@Dao
abstract class AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(item: Album)

    @Query("SELECT * FROM Album")
    abstract suspend fun find() : Album?

    @Query("SELECT * FROM Album ORDER BY createdTime DESC")
    abstract fun findAll() : LiveData<List<Album>>

    @Query("DELETE FROM Album")
    abstract suspend fun clear()
}