package dev.wceng.sufei.data.local.datastore

import androidx.datastore.core.DataStore
import dev.wceng.sufei.data.model.ChineseVariant
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
                favorites = proto.favoritesMap,
                fontSizeMultiplier = if (proto.fontSizeMultiplier == 0f) 1.0f else proto.fontSizeMultiplier,
                lineHeightMultiplier = if (proto.lineHeightMultiplier == 0f) 1.0f else proto.lineHeightMultiplier,
                useDynamicColor = proto.useDynamicColor,
                fontFamilyName = proto.fontFamilyName.ifEmpty { "Serif" },
                dailyPoemId = proto.dailyPoemId,
                lastUpdateMillis = proto.lastUpdateMillis,
                pinnedWidgetPoems = proto.pinnedWidgetPoemsMap.mapKeys { it.key.toInt() },
                chineseVariant = when (proto.chineseVariant) {
                    ChineseVariantProto.SIMPLIFIED -> ChineseVariant.SIMPLIFIED
                    ChineseVariantProto.TRADITIONAL_HK -> ChineseVariant.TRADITIONAL_HK
                    ChineseVariantProto.TRADITIONAL_TW -> ChineseVariant.TRADITIONAL_TW
                    else -> detectDefaultVariant()
                }
            )
        }

    private fun detectDefaultVariant(): ChineseVariant {
        val locale = java.util.Locale.getDefault()
        return when (locale.language) {
            "zh" -> when (locale.country) {
                "HK", "MO" -> ChineseVariant.TRADITIONAL_HK
                "TW" -> ChineseVariant.TRADITIONAL_TW
                else -> ChineseVariant.SIMPLIFIED
            }
            else -> ChineseVariant.SIMPLIFIED
        }
    }

    /**
     * 更新收藏状态
     */
    suspend fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        updateData { currentPrefs ->
            val favoritesBuilder = currentPrefs.favoritesMap.toMutableMap()
            if (isFavorite) {
                favoritesBuilder[poemId] = System.currentTimeMillis()
            } else {
                favoritesBuilder.remove(poemId)
            }
            currentPrefs.toBuilder()
                .clearFavorites()
                .putAllFavorites(favoritesBuilder)
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

    suspend fun setChineseVariant(variant: ChineseVariant) {
        updateData {
            it.toBuilder()
                .setChineseVariant(
                    when (variant) {
                        ChineseVariant.TRADITIONAL_HK -> ChineseVariantProto.TRADITIONAL_HK
                        ChineseVariant.TRADITIONAL_TW -> ChineseVariantProto.TRADITIONAL_TW
                        ChineseVariant.SIMPLIFIED -> ChineseVariantProto.SIMPLIFIED
                    }
                )
                .build()
        }
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

    /**
     * 解析 widget 实例对应的诗词 ID。
     * 原子操作：若 appWidgetId 尚未分配且 pendingPoemId 不为空，则写入 map。
     */
    suspend fun resolveWidgetPoemId(appWidgetId: Int, pendingPoemId: String?): String? {
        return try {
            val updatedPrefs = userPreferencesStore.updateData { prefs ->
                val existing = prefs.pinnedWidgetPoemsMap[appWidgetId.toLong()]
                if (!existing.isNullOrEmpty()) {
                    return@updateData prefs
                }
                if (!pendingPoemId.isNullOrEmpty()) {
                    prefs.toBuilder()
                        .putPinnedWidgetPoems(appWidgetId.toLong(), pendingPoemId)
                        .build()
                } else {
                    prefs
                }
            }
            val poemId = updatedPrefs.pinnedWidgetPoemsMap[appWidgetId.toLong()]
            if (poemId.isNullOrEmpty()) null else poemId
        } catch (ioException: IOException) {
            null
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
