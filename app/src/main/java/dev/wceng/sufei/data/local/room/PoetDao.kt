package dev.wceng.sufei.data.local.room

import androidx.room.*
import dev.wceng.sufei.data.local.room.entity.PoetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoetDao {
    @Query("SELECT * FROM poets ORDER BY poemCount DESC")
    fun getAllPoets(): Flow<List<PoetEntity>>

    @Query("SELECT * FROM poets WHERE id = :id")
    fun getPoetByIdFlow(id: String): Flow<PoetEntity?>

    @Query("SELECT * FROM poets WHERE id = :id")
    suspend fun getPoetById(id: String): PoetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoets(poets: List<PoetEntity>)

    @Query("SELECT COUNT(*) FROM poets")
    suspend fun getPoetCount(): Int

    @Query("SELECT * FROM poets WHERE name LIKE '%' || :query || '%' LIMIT :limit")
    fun searchPoetsByName(query: String, limit: Int = 20): Flow<List<PoetEntity>>
}
