package dev.wceng.sufei.data.local.datastore

import androidx.datastore.core.DataMigration

/**
 * 将旧的 favorite_poem_ids 列表迁移到新的 favorites Map 中
 */
object FavoritesMigration : DataMigration<UserPreferences> {
    override suspend fun shouldMigrate(currentData: UserPreferences): Boolean {
        // 如果旧列表不为空，且新 Map 为空，则需要迁移
        return currentData.favoritePoemIdsList.isNotEmpty() && currentData.favoritesMap.isEmpty()
    }

    override suspend fun migrate(currentData: UserPreferences): UserPreferences {
        val favorites = currentData.favoritePoemIdsList.associateWith { 0L }
        return currentData.toBuilder()
            .putAllFavorites(favorites)
            .clearFavoritePoemIds()
            .build()
    }

    override suspend fun cleanUp() {
        // No-op
    }
}
