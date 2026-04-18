package dev.wceng.sufei.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.wceng.sufei.data.model.Poem

/**
 * 数据库实体类 (Room 内部使用)
 * 仅存储诗词基础数据，不包含收藏状态
 */
@Entity(tableName = "poems")
data class PoemEntity(
    @PrimaryKey
    val id: String,              // UUID
    val sourceUrl: String,       // 原始数据的 URL
    val title: String,           // 标题
    val author: String,          // 作者
    val dynasty: String,         // 朝代
    val content: String,         // 正文
    val tags: List<String>,      // 标签
    val notes: String? = null,   // 注释
    val translation: String? = null, // 译文
    val intro: String? = null,   // 赏析
    val background: String? = null // 创作背景
)

/**
 * 将数据库实体转换为领域模型
 */
fun PoemEntity.toPoem(): Poem {
    return Poem(
        id = id,
        sourceUrl = sourceUrl,
        title = title,
        author = author,
        dynasty = dynasty,
        content = content,
        tags = tags,
        notes = notes,
        translation = translation,
        intro = intro,
        background = background
    )
}

/**
 * 将领域模型转换为数据库实体
 */
fun Poem.toEntity(): PoemEntity {
    return PoemEntity(
        id = id,
        sourceUrl = sourceUrl,
        title = title,
        author = author,
        dynasty = dynasty,
        content = content,
        tags = tags,
        notes = notes,
        translation = translation,
        intro = intro,
        background = background
    )
}
