package dev.wceng.sufei.data.local.room.entity

import dev.wceng.sufei.data.model.Poet

/**
 * 将数据库实体转换为领域模型
 */
fun PoetEntity.toPoet(): Poet {
    return Poet(
        id = id,
        name = name,
        dynasty = dynasty,
        avatarUrl = avatarUrl,
        lifetime = lifetime,
        descriptions = descriptions,
        poemCount = poemCount
    )
}

/**
 * 将领域模型转换为数据库实体
 */
fun Poet.toEntity(): PoetEntity {
    return PoetEntity(
        id = id,
        name = name,
        dynasty = dynasty,
        avatarUrl = avatarUrl,
        lifetime = lifetime,
        descriptions = descriptions,
        poemCount = poemCount
    )
}
