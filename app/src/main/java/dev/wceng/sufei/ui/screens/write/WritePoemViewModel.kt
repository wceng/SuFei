package dev.wceng.sufei.ui.screens.write

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.repository.PoemRepository
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 撰写诗词页 ViewModel：保存 = 创作诗写入 poems 表 + DataStore 登记创作标记
 */
@HiltViewModel
class WritePoemViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun savePoem(title: String, content: String, tuneName: String?) {
        viewModelScope.launch {
            val id = poemRepository.saveUserPoem(title, content, tuneName)
            userPreferencesRepository.addUserPoem(id)
        }
    }
}
