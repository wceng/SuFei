package dev.wceng.sufei.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.wceng.sufei.data.model.Tag

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val name: String
)

fun TagEntity.toTag() = Tag(name = name)
fun Tag.toEntity() = TagEntity(name = name)
