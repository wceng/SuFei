package dev.wceng.sufei.domain.model

import dev.wceng.sufei.data.model.UserPoem

/**
 * 分组后的用户诗词模型
 */
data class GroupedUserPoem(
    val dateLabel: String,
    val poems: List<UserPoem>
)
