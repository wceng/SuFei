package dev.wceng.sufei.data.repository

import dev.wceng.sufei.data.local.datastore.UserPreferencesDataSource
import dev.wceng.sufei.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataSource.userPreferencesFlow

    override suspend fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        dataSource.toggleFavorite(poemId, isFavorite)
    }

    override suspend fun setFontSizeMultiplier(multiplier: Float) {
        dataSource.setFontSizeMultiplier(multiplier)
    }

    override suspend fun setLineHeightMultiplier(multiplier: Float) {
        dataSource.setLineHeightMultiplier(multiplier)
    }

    override suspend fun setUseDynamicColor(use: Boolean) {
        dataSource.setUseDynamicColor(use)
    }

    override suspend fun setFontFamilyName(name: String) {
        dataSource.setFontFamilyName(name)
    }

    override suspend fun updateDailyPoem(poemId: String, timestamp: Long) {
        dataSource.updateDailyPoem(poemId, timestamp)
    }
}
