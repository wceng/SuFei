package dev.wceng.sufei.data.model

import kotlinx.serialization.Serializable

/**
 * 诗人领域模型 (Domain Model)
 */
@Serializable
data class Poet(
    val id: String,                 // 对应 onlyId
    val name: String,               // 姓名
    val dynasty: String,            // 朝代
    val avatarUrl: String? = null,  // 头像链接
    val lifetime: String? = null,   // 简要生平描述 (一句话简介)
    val descriptions: List<PoetDescription> = emptyList(), // 详细描述集合 (生平、成就、评价等)
    val poemCount: Int = 0          // 诗词作品数量
)

/**
 * 诗人详细描述项
 */
@Serializable
data class PoetDescription(
    val type: String,    // 描述类型，如“生平”、“成就”、“轶事典故”
    val content: String  // 清洗后的内容文本
)
