package dev.wceng.sufei.data.repository

import dev.wceng.sufei.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    
    suspend fun toggleFavorite(poemId: String, isFavorite: Boolean)
    suspend fun setFontSizeMultiplier(multiplier: Float)
    suspend fun setLineHeightMultiplier(multiplier: Float)
    suspend fun setUseDynamicColor(use: Boolean)
    suspend fun setFontFamilyName(name: String)
}
