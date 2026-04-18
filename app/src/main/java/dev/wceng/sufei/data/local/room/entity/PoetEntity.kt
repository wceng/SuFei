package dev.wceng.sufei.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.wceng.sufei.data.model.PoetDescription

/**
 * 诗人数据库实体
 */
@Entity(tableName = "poets")
data class PoetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val dynasty: String,
    val avatarUrl: String?,
    val lifetime: String?,
    val descriptions: List<PoetDescription>,
    val poemCount: Int
)
