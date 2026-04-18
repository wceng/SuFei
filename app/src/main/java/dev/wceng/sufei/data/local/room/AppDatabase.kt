package dev.wceng.sufei.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.wceng.sufei.data.local.room.entity.PoemEntity
import dev.wceng.sufei.data.local.room.entity.PoetEntity
import dev.wceng.sufei.data.local.room.entity.TagEntity
import dev.wceng.sufei.data.local.room.entity.TuneEntity

@Database(
    entities = [
        PoemEntity::class, 
        TagEntity::class, 
        PoetEntity::class, 
        TuneEntity::class
    ],
    version = 1, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poemDao(): PoemDao
    abstract fun tagDao(): TagDao
    abstract fun poetDao(): PoetDao
    abstract fun tuneDao(): TuneDao
}
