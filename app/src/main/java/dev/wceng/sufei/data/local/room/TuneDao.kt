package dev.wceng.sufei.data.local.room

import androidx.room.*
import dev.wceng.sufei.data.local.room.entity.TuneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TuneDao {
    @Query("SELECT * FROM tunes ORDER BY name ASC")
    fun getAllTunes(): Flow<List<TuneEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTunes(tunes: List<TuneEntity>)

    @Query("SELECT COUNT(*) FROM tunes")
    suspend fun getTuneCount(): Int
}
