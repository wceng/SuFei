package dev.wceng.sufei.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.wceng.sufei.data.local.room.entity.PoemEntity
import dev.wceng.sufei.data.local.room.entity.PoetEntity
import dev.wceng.sufei.data.local.room.entity.TagEntity
import dev.wceng.sufei.data.local.room.entity.TuneEntity
import dev.wceng.sufei.util.cleanAuthor
import dev.wceng.sufei.util.cleanDescription
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Database(
    entities = [
        PoemEntity::class, 
        TagEntity::class, 
        PoetEntity::class, 
        TuneEntity::class
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poemDao(): PoemDao
    abstract fun tagDao(): TagDao
    abstract fun poetDao(): PoetDao
    abstract fun tuneDao(): TuneDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 清理标题中的斜杠脏数据
                db.execSQL("""
                    UPDATE poems 
                    SET title = TRIM(SUBSTR(title, 1, INSTR(title, '/') - 1)) 
                    WHERE title LIKE '%/%'
                """)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 占位迁移
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 占位迁移
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 占位迁移
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                println("Migration 5-6 starting: Cleaning author field...")
                var totalUpdated = 0
                while (true) {
                    val dirtyData = mutableListOf<Pair<String, String>>()
                    val cursor = db.query(
                        "SELECT id, author FROM poems WHERE author LIKE '%撰%' LIMIT 500"
                    )

                    var count = 0
                    if (cursor.moveToFirst()) {
                        val idColIndex = cursor.getColumnIndex("id")
                        val authorIndex = cursor.getColumnIndex("author")
                        do {
                            dirtyData.add(cursor.getString(idColIndex) to cursor.getString(authorIndex))
                            count++
                        } while (cursor.moveToNext())
                    }
                    cursor.close()

                    dirtyData.forEach { (id, author) ->
                        val cleanedAuthor = author.cleanAuthor()
                        if (author != cleanedAuthor) {
                            db.execSQL(
                                "UPDATE poems SET author = ? WHERE id = ?",
                                arrayOf(cleanedAuthor, id)
                            )
                            totalUpdated++
                        }
                    }
                    if (count < 500) break
                }
                println("Migration 5-6 complete. Total cleaned: $totalUpdated")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                println("Migration 6-7 starting: Cleaning poet descriptions...")
                val json = Json { ignoreUnknownKeys = true }
                val cursor = db.query("SELECT id, descriptions FROM poets")
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex("id")
                    val descIndex = cursor.getColumnIndex("descriptions")
                    do {
                        val id = cursor.getString(idIndex)
                        val descJson = cursor.getString(descIndex)
                        try {
                            val descriptions = json.decodeFromString<List<dev.wceng.sufei.data.model.PoetDescription>>(descJson)
                            val cleanedDescriptions = descriptions.map { 
                                it.copy(content = it.content.cleanDescription()) 
                            }
                            if (descriptions != cleanedDescriptions) {
                                val newJson = json.encodeToString(cleanedDescriptions)
                                db.execSQL("UPDATE poets SET descriptions = ? WHERE id = ?", arrayOf(newJson, id))
                            }
                        } catch (e: Exception) {
                            println("Migration 6-7 error for poet $id: ${e.message}")
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()
                println("Migration 6-7 complete.")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 占位迁移
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                println("Migration 8-9 starting: Remedial cleaning for missed poem content...")
                var totalUpdated = 0
                while (true) {
                    val dirtyData = mutableListOf<Pair<String, String>>()
                    val cursor = db.query(
                        "SELECT id, content FROM poems WHERE content LIKE '%（%' OR content LIKE '%(%' LIMIT 500"
                    )

                    var count = 0
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndex("id")
                        val contentIndex = cursor.getColumnIndex("content")
                        do {
                            dirtyData.add(cursor.getString(idIndex) to cursor.getString(contentIndex))
                            count++
                        } while (cursor.moveToNext())
                    }
                    cursor.close()

                    dirtyData.forEach { (id, content) ->
                        val cleanedContent = dev.wceng.sufei.util.cleanPoemContent(content)
                        if (content != cleanedContent) {
                            println("Migration 8-9: UPDATING ID $id")
                            db.execSQL(
                                "UPDATE poems SET content = ? WHERE id = ?",
                                arrayOf(cleanedContent, id)
                            )
                            totalUpdated++
                        }
                    }

                    if (count < 500) break
                }
                println("Migration 8-9 complete. Total cleaned: $totalUpdated")
            }
        }
    }
}
