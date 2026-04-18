package dev.wceng.sufei.data.local.datastore

import androidx.datastore.core.DataStore
import dev.wceng.sufei.data.model.UserPreferences as UserPreferencesModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * 用户偏好数据源，负责与 Proto DataStore 交互
 */
class UserPreferencesDataSource(
    private val userPreferencesStore: DataStore<UserPreferences>
) {
    // 映射 Proto 对象到业务模型
    val userPreferencesFlow: Flow<UserPreferencesModel> = userPreferencesStore.data
        .map { proto ->
            UserPreferencesModel(
                favoritePoemIds = proto.favoritePoemIdsList.toSet(),
                fontSizeMultiplier = if (proto.fontSizeMultiplier == 0f) 1.0f else proto.fontSizeMultiplier,
                lineHeightMultiplier = if (proto.lineHeightMultiplier == 0f) 1.0f else proto.lineHeightMultiplier,
                useDynamicColor = proto.useDynamicColor,
                fontFamilyName = proto.fontFamilyName.ifEmpty { "Serif" },
                dailyPoemId = proto.dailyPoemId,
                lastUpdateMillis = proto.lastUpdateMillis
            )
        }

    /**
     * 更新收藏状态
     */
    suspend fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        updateData { currentPrefs ->
            val currentIds = currentPrefs.favoritePoemIdsList.toSet()
            val newIds = if (isFavorite) {
                currentIds + poemId
            } else {
                currentIds - poemId
            }
            currentPrefs.toBuilder()
                .clearFavoritePoemIds()
                .addAllFavoritePoemIds(newIds)
                .build()
        }
    }

    suspend fun setFontSizeMultiplier(multiplier: Float) {
        updateData { it.toBuilder().setFontSizeMultiplier(multiplier).build() }
    }

    suspend fun setLineHeightMultiplier(multiplier: Float) {
        updateData { it.toBuilder().setLineHeightMultiplier(multiplier).build() }
    }

    suspend fun setUseDynamicColor(use: Boolean) {
        updateData { it.toBuilder().setUseDynamicColor(use).build() }
    }

    suspend fun setFontFamilyName(name: String) {
        updateData { it.toBuilder().setFontFamilyName(name).build() }
    }

    /**
     * 更新每日诗词
     */
    suspend fun updateDailyPoem(poemId: String, timestamp: Long) {
        updateData { 
            it.toBuilder()
                .setDailyPoemId(poemId)
                .setLastUpdateMillis(timestamp)
                .build() 
        }
    }

    private suspend fun updateData(transform: (UserPreferences) -> UserPreferences) {
        try {
            userPreferencesStore.updateData { transform(it) }
        } catch (ioException: IOException) {
            // Log or handle error
        }
    }
}
