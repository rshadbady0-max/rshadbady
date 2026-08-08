package com.example.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Word
import com.example.data.repository.EnglishMasteryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ViewMode { LIST, FLASHCARD }

data class VocabularyUiState(
    val words: List<Word> = emptyList(),
    val filteredWords: List<Word> = emptyList(),
    val selectedLevelFilter: String = "ALL", // "ALL", "A1-A2", "B1-B2", "C1-C2"
    val selectedCategoryFilter: String = "ALL", // "ALL", "Family", "Food", etc.
    val searchQuery: String = "",
    val viewMode: ViewMode = ViewMode.FLASHCARD,
    val currentFlashcardIndex: Int = 0,
    val isCardFlipped: Boolean = false,
    val categories: List<String> = emptyList()
)

private data class FilterConfig(
    val level: String,
    val category: String,
    val query: String
)

private data class CardConfig(
    val mode: ViewMode,
    val cardIndex: Int,
    val isFlipped: Boolean
)

class VocabularyViewModel(
    private val repository: EnglishMasteryRepository
) : ViewModel() {

    private val _selectedLevel = MutableStateFlow("ALL")
    private val _selectedCategory = MutableStateFlow("ALL")
    private val _searchQuery = MutableStateFlow("")
    private val _viewMode = MutableStateFlow(ViewMode.FLASHCARD)
    private val _currentFlashcardIndex = MutableStateFlow(0)
    private val _isCardFlipped = MutableStateFlow(false)

    private val _filterConfig = combine(_selectedLevel, _selectedCategory, _searchQuery) { level, cat, query ->
        FilterConfig(level, cat, query)
    }

    private val _cardConfig = combine(_viewMode, _currentFlashcardIndex, _isCardFlipped) { mode, idx, flipped ->
        CardConfig(mode, idx, flipped)
    }

    val uiState: StateFlow<VocabularyUiState> = combine(
        repository.allWords,
        _filterConfig,
        _cardConfig
    ) { allWords, filters, cardState ->

        val categoriesList = listOf("ALL") + allWords.map { it.category }.distinct().sorted()

        var filtered = allWords
        if (filters.level != "ALL") {
            filtered = filtered.filter { it.level == filters.level }
        }
        if (filters.category != "ALL") {
            filtered = filtered.filter { it.category == filters.category }
        }
        if (filters.query.isNotBlank()) {
            val q = filters.query.trim().lowercase()
            filtered = filtered.filter {
                it.englishWord.lowercase().contains(q) || it.arabicTranslation.contains(q)
            }
        }

        val safeIndex = if (filtered.isEmpty()) 0 else cardState.cardIndex.coerceIn(0, filtered.size - 1)

        VocabularyUiState(
            words = allWords,
            filteredWords = filtered,
            selectedLevelFilter = filters.level,
            selectedCategoryFilter = filters.category,
            searchQuery = filters.query,
            viewMode = cardState.mode,
            currentFlashcardIndex = safeIndex,
            isCardFlipped = cardState.isFlipped,
            categories = categoriesList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VocabularyUiState()
    )

    fun setLevelFilter(level: String) {
        _selectedLevel.value = level
        _currentFlashcardIndex.value = 0
        _isCardFlipped.value = false
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
        _currentFlashcardIndex.value = 0
        _isCardFlipped.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _currentFlashcardIndex.value = 0
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun toggleCardFlip() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun nextCard() {
        val total = uiState.value.filteredWords.size
        if (total > 0) {
            _currentFlashcardIndex.value = (_currentFlashcardIndex.value + 1) % total
            _isCardFlipped.value = false
        }
    }

    fun previousCard() {
        val total = uiState.value.filteredWords.size
        if (total > 0) {
            val prev = if (_currentFlashcardIndex.value - 1 < 0) total - 1 else _currentFlashcardIndex.value - 1
            _currentFlashcardIndex.value = prev
            _isCardFlipped.value = false
        }
    }

    fun toggleLearned(word: Word) {
        viewModelScope.launch {
            repository.toggleLearned(word)
        }
    }

    fun toggleMastered(word: Word) {
        viewModelScope.launch {
            repository.toggleMastered(word)
        }
    }

    fun speak(text: String) {
        repository.speakWord(text)
    }

    class Factory(private val repository: EnglishMasteryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VocabularyViewModel(repository) as T
        }
    }
}
