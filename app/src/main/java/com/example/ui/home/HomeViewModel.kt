package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Word
import com.example.data.repository.EnglishMasteryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalWordsCount: Int = 0,
    val learnedCount: Int = 0,
    val masteredCount: Int = 0,
    val a1Learned: Int = 0,
    val a1Total: Int = 0,
    val b1Learned: Int = 0,
    val b1Total: Int = 0,
    val c1Learned: Int = 0,
    val c1Total: Int = 0,
    val streakDays: Int = 1,
    val recommendedWords: List<Word> = emptyList(),
    val isTtsReady: Boolean = false
)

class HomeViewModel(
    private val repository: EnglishMasteryRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allWords,
        repository.learnedWords,
        repository.masteredWords,
        repository.preferencesManager.streakDays,
        repository.ttsManager.isReady
    ) { all, learned, mastered, streak, ttsReady ->
        val a1List = all.filter { it.level == "A1-A2" }
        val b1List = all.filter { it.level == "B1-B2" }
        val c1List = all.filter { it.level == "C1-C2" }

        val unlearned = all.filter { !it.isLearned }
        val recs = if (unlearned.isNotEmpty()) unlearned.take(5) else all.take(5)

        HomeUiState(
            totalWordsCount = all.size,
            learnedCount = learned.size,
            masteredCount = mastered.size,
            a1Learned = a1List.count { it.isLearned },
            a1Total = a1List.size,
            b1Learned = b1List.count { it.isLearned },
            b1Total = b1List.size,
            c1Learned = c1List.count { it.isLearned },
            c1Total = c1List.size,
            streakDays = streak,
            recommendedWords = recs,
            isTtsReady = ttsReady
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun speak(text: String) {
        repository.speakWord(text)
    }

    class Factory(private val repository: EnglishMasteryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
