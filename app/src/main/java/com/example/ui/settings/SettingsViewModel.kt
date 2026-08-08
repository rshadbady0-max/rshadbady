package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EnglishMasteryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val speechRate: Float = 1.0f,
    val isTtsReady: Boolean = false,
    val isResetDialogOpen: Boolean = false
)

class SettingsViewModel(
    private val repository: EnglishMasteryRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.preferencesManager.isDarkMode,
        repository.preferencesManager.speechRate,
        repository.ttsManager.isReady
    ) { dark, rate, ttsReady ->
        SettingsUiState(
            isDarkMode = dark,
            speechRate = rate,
            isTtsReady = ttsReady
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setDarkMode(enabled: Boolean) {
        repository.preferencesManager.setDarkMode(enabled)
    }

    fun setSpeechRate(rate: Float) {
        repository.updateSpeechRate(rate)
    }

    fun testTtsSound() {
        repository.speakWord("Hello! This is a test of English pronunciation.")
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllProgress()
        }
    }

    class Factory(private val repository: EnglishMasteryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
