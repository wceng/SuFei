package dev.wceng.sufei.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.ChineseVariant
import dev.wceng.sufei.data.model.UserPreferences
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setFontSizeMultiplier(multiplier: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setFontSizeMultiplier(multiplier)
        }
    }

    fun setLineHeightMultiplier(multiplier: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setLineHeightMultiplier(multiplier)
        }
    }

    fun setUseDynamicColor(use: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setUseDynamicColor(use)
        }
    }

    fun setFontFamilyName(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.setFontFamilyName(name)
        }
    }

    fun setChineseVariant(variant: ChineseVariant) {
        viewModelScope.launch {
            // 1. 更新持久化存储 (用于内容转换)
            userPreferencesRepository.setChineseVariant(variant)

            // 2. 立即应用 UI 语言
            val localeTag = when (variant) {
                ChineseVariant.SIMPLIFIED -> "zh-Hans-CN"
                ChineseVariant.TRADITIONAL_HK -> "zh-Hant-HK"
                ChineseVariant.TRADITIONAL_TW -> "zh-Hant-TW"
            }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
        }
    }
}
