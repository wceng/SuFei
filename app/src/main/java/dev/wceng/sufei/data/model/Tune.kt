package dev.wceng.sufei.data.model

import kotlinx.serialization.Serializable

/**
 * 词牌领域模型
 */
@Serializable
data class Tune(
    val name: String,
    val description: String? = null
)
