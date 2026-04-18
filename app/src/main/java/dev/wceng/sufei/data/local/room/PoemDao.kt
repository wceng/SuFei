package dev.wceng.sufei.data.local.room

import androidx.room.*
import dev.wceng.sufei.data.local.room.entity.PoemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoemDao {
    @Query("SELECT * FROM poems LIMIT :limit")
    fun getAllPoems(limit: Int): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE id = :id")
    fun getPoemByIdFlow(id: String): Flow<PoemEntity?>

    @Query("SELECT * FROM poems WHERE id = :id")
    suspend fun getPoemById(id: String): PoemEntity?

    @Query("SELECT * FROM poems WHERE id IN (:ids)")
    fun getPoemsByIds(ids: Collection<String>): Flow<List<PoemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoems(poems: List<PoemEntity>)

    @Query("SELECT COUNT(*) FROM poems")
    suspend fun getPoemCount(): Int

    @Query("""
        SELECT * FROM poems 
        WHERE (:dynasty IS NULL OR dynasty = :dynasty)
        AND (:tag IS NULL OR tags LIKE '%' || :tag || '%')
        AND (:tune IS NULL OR title LIKE '%' || :tune || '%')
        AND (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        LIMIT :limit
    """)
    fun searchPoems(query: String, dynasty: String?, tag: String?, tune: String?, limit: Int = 50): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE author = :authorName LIMIT :limit")
    fun getPoemsByAuthor(authorName: String, limit: Int = 20): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems ORDER BY RANDOM() LIMIT 1")
    fun getRandomPoem(): Flow<PoemEntity?>
}
