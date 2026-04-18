package dev.wceng.sufei.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
}
