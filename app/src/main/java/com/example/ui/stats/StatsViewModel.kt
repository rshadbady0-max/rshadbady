package com.example.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.TestResult
import com.example.data.repository.EnglishMasteryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val totalWords: Int = 0,
    val totalLearned: Int = 0,
    val totalMastered: Int = 0,
    val a1Learned: Int = 0,
    val a1Total: Int = 0,
    val b1Learned: Int = 0,
    val b1Total: Int = 0,
    val c1Learned: Int = 0,
    val c1Total: Int = 0,
    val testResultsHistory: List<TestResult> = emptyList(),
    val averageScorePercentage: Int = 0
)

class StatsViewModel(
    private val repository: EnglishMasteryRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.allWords,
        repository.learnedWords,
        repository.masteredWords,
        repository.testResults
    ) { all, learned, mastered, results ->
        val a1List = all.filter { it.level == "A1-A2" }
        val b1List = all.filter { it.level == "B1-B2" }
        val c1List = all.filter { it.level == "C1-C2" }

        val avgScore = if (results.isNotEmpty()) {
            results.map { (it.score.toFloat() / it.totalQuestions.coerceAtLeast(1)) * 100 }.average().toInt()
        } else {
            0
        }

        StatsUiState(
            totalWords = all.size,
            totalLearned = learned.size,
            totalMastered = mastered.size,
            a1Learned = a1List.count { it.isLearned },
            a1Total = a1List.size,
            b1Learned = b1List.count { it.isLearned },
            b1Total = b1List.size,
            c1Learned = c1List.count { it.isLearned },
            c1Total = c1List.size,
            testResultsHistory = results,
            averageScorePercentage = avgScore
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    class Factory(private val repository: EnglishMasteryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(repository) as T
        }
    }
}
