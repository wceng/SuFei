package dev.wceng.sufei.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.wceng.sufei.data.model.Tune

/**
 * 词牌数据库实体
 */
@Entity(tableName = "tunes")
data class TuneEntity(
    @PrimaryKey
    val name: String,
    val description: String?
)

fun TuneEntity.toTune() = Tune(name = name, description = description)
fun Tune.toEntity() = TuneEntity(name = name, description = description)
