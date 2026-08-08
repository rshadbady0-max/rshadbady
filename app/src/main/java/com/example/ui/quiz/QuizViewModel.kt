package com.example.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Word
import com.example.data.repository.EnglishMasteryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class QuizType { MCQ, SPELLING, LISTENING }

data class QuizQuestion(
    val word: Word,
    val options: List<String> = emptyList(), // For MCQ & Listening
    val correctAnswer: String = ""
)

data class QuestionResult(
    val question: QuizQuestion,
    val userAnswer: String,
    val isCorrect: Boolean
)

enum class QuizState { SETUP, ACTIVE, COMPLETED }

data class QuizUiState(
    val quizState: QuizState = QuizState.SETUP,
    val quizType: QuizType = QuizType.MCQ,
    val selectedLevel: String = "ALL", // "ALL", "A1-A2", "B1-B2", "C1-C2"
    val questionCount: Int = 10,
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val userAnswers: List<QuestionResult> = emptyList(),
    val selectedAnswer: String? = null,
    val textAnswerInput: String = "",
    val isAnswerSubmitted: Boolean = false,
    val score: Int = 0
)

private data class SetupConfig(
    val state: QuizState,
    val type: QuizType,
    val level: String,
    val count: Int
)

private data class QuestionProgress(
    val questions: List<QuizQuestion>,
    val index: Int,
    val userAnswers: List<QuestionResult>
)

private data class AnswerProgress(
    val selectedAns: String?,
    val textInput: String,
    val submitted: Boolean,
    val score: Int
)

class QuizViewModel(
    private val repository: EnglishMasteryRepository
) : ViewModel() {

    private val _quizState = MutableStateFlow(QuizState.SETUP)
    private val _quizType = MutableStateFlow(QuizType.MCQ)
    private val _selectedLevel = MutableStateFlow("ALL")
    private val _questionCount = MutableStateFlow(10)
    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    private val _currentQuestionIndex = MutableStateFlow(0)
    private val _userAnswers = MutableStateFlow<List<QuestionResult>>(emptyList())
    private val _selectedAnswer = MutableStateFlow<String?>(null)
    private val _textAnswerInput = MutableStateFlow("")
    private val _isAnswerSubmitted = MutableStateFlow(false)
    private val _score = MutableStateFlow(0)

    private val _setupConfig = combine(_quizState, _quizType, _selectedLevel, _questionCount) { state, type, level, count ->
        SetupConfig(state, type, level, count)
    }

    private val _questionProgress = combine(_questions, _currentQuestionIndex, _userAnswers) { qList, idx, answers ->
        QuestionProgress(qList, idx, answers)
    }

    private val _answerProgress = combine(_selectedAnswer, _textAnswerInput, _isAnswerSubmitted, _score) { sel, txt, sub, score ->
        AnswerProgress(sel, txt, sub, score)
    }

    val uiState: StateFlow<QuizUiState> = combine(_setupConfig, _questionProgress, _answerProgress) { config, qProg, aProg ->
        QuizUiState(
            quizState = config.state,
            quizType = config.type,
            selectedLevel = config.level,
            questionCount = config.count,
            questions = qProg.questions,
            currentQuestionIndex = qProg.index,
            userAnswers = qProg.userAnswers,
            selectedAnswer = aProg.selectedAns,
            textAnswerInput = aProg.textInput,
            isAnswerSubmitted = aProg.submitted,
            score = aProg.score
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuizUiState()
    )

    fun setQuizType(type: QuizType) {
        _quizType.value = type
    }

    fun setSelectedLevel(level: String) {
        _selectedLevel.value = level
    }

    fun setQuestionCount(count: Int) {
        _questionCount.value = count
    }

    fun startQuiz() {
        viewModelScope.launch {
            val allWords = repository.allWords.first()
            var pool = if (_selectedLevel.value != "ALL") {
                allWords.filter { it.level == _selectedLevel.value }
            } else {
                allWords
            }
            if (pool.size < 4) {
                pool = allWords
            }

            val shuffledWords = pool.shuffled().take(_questionCount.value)
            val generatedQuestions = shuffledWords.map { word ->
                when (_quizType.value) {
                    QuizType.MCQ, QuizType.LISTENING -> {
                        val wrongChoices = allWords.filter { it.id != word.id }
                            .shuffled()
                            .take(3)
                            .map { it.arabicTranslation }
                        val options = (wrongChoices + word.arabicTranslation).shuffled()
                        QuizQuestion(
                            word = word,
                            options = options,
                            correctAnswer = word.arabicTranslation
                        )
                    }
                    QuizType.SPELLING -> {
                        QuizQuestion(
                            word = word,
                            correctAnswer = word.englishWord.trim().lowercase()
                        )
                    }
                }
            }

            _questions.value = generatedQuestions
            _currentQuestionIndex.value = 0
            _userAnswers.value = emptyList()
            _selectedAnswer.value = null
            _textAnswerInput.value = ""
            _isAnswerSubmitted.value = false
            _score.value = 0
            _quizState.value = QuizState.ACTIVE

            if (_quizType.value == QuizType.LISTENING && generatedQuestions.isNotEmpty()) {
                playCurrentAudio()
            }
        }
    }

    fun onOptionSelected(option: String) {
        if (!_isAnswerSubmitted.value) {
            _selectedAnswer.value = option
        }
    }

    fun onTextInputChanged(input: String) {
        if (!_isAnswerSubmitted.value) {
            _textAnswerInput.value = input
        }
    }

    fun submitAnswer() {
        if (_isAnswerSubmitted.value) return

        val currentQ = _questions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val userAnswer = when (_quizType.value) {
            QuizType.MCQ, QuizType.LISTENING -> _selectedAnswer.value ?: ""
            QuizType.SPELLING -> _textAnswerInput.value.trim().lowercase()
        }

        val isCorrect = userAnswer.equals(currentQ.correctAnswer, ignoreCase = true)

        if (isCorrect) {
            _score.value = _score.value + 1
        }

        val updatedAnswers = _userAnswers.value + QuestionResult(
            question = currentQ,
            userAnswer = userAnswer,
            isCorrect = isCorrect
        )
        _userAnswers.value = updatedAnswers
        _isAnswerSubmitted.value = true
    }

    fun nextQuestion() {
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < _questions.value.size) {
            _currentQuestionIndex.value = nextIdx
            _selectedAnswer.value = null
            _textAnswerInput.value = ""
            _isAnswerSubmitted.value = false

            if (_quizType.value == QuizType.LISTENING) {
                playCurrentAudio()
            }
        } else {
            finishQuiz()
        }
    }

    fun playCurrentAudio() {
        val currentQ = _questions.value.getOrNull(_currentQuestionIndex.value)
        if (currentQ != null) {
            repository.speakWord(currentQ.word.englishWord)
        }
    }

    private fun finishQuiz() {
        _quizState.value = QuizState.COMPLETED
        viewModelScope.launch {
            repository.saveTestResult(
                categoryOrLevel = _selectedLevel.value,
                score = _score.value,
                total = _questions.value.size,
                testType = _quizType.value.name
            )
        }
    }

    fun resetQuizToSetup() {
        _quizState.value = QuizState.SETUP
        _selectedAnswer.value = null
        _textAnswerInput.value = ""
        _isAnswerSubmitted.value = false
    }

    class Factory(private val repository: EnglishMasteryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuizViewModel(repository) as T
        }
    }
}
