package com.example.data.repository

import com.example.data.dao.TestResultDao
import com.example.data.dao.WordDao
import com.example.data.database.SeedData
import com.example.data.model.TestResult
import com.example.data.model.Word
import com.example.data.util.TtsManager
import com.example.util.PreferencesManager
import kotlinx.coroutines.flow.Flow

class EnglishMasteryRepository(
    private val wordDao: WordDao,
    private val testResultDao: TestResultDao,
    val ttsManager: TtsManager,
    val preferencesManager: PreferencesManager
) {
    val allWords: Flow<List<Word>> = wordDao.getAllWords()
    val learnedWords: Flow<List<Word>> = wordDao.getLearnedWords()
    val masteredWords: Flow<List<Word>> = wordDao.getMasteredWords()
    val testResults: Flow<List<TestResult>> = testResultDao.getAllResults()

    fun getWordsByLevel(level: String): Flow<List<Word>> = wordDao.getWordsByLevel(level)

    fun getWordsByCategory(category: String): Flow<List<Word>> = wordDao.getWordsByCategory(category)

    suspend fun toggleLearned(word: Word) {
        val newLearned = !word.isLearned
        wordDao.updateWordStatus(
            id = word.id,
            isLearned = newLearned,
            isMastered = if (!newLearned) false else word.isMastered,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun toggleMastered(word: Word) {
        val newMastered = !word.isMastered
        wordDao.updateWordStatus(
            id = word.id,
            isLearned = if (newMastered) true else word.isLearned,
            isMastered = newMastered,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun saveTestResult(categoryOrLevel: String, score: Int, total: Int, testType: String) {
        testResultDao.insertResult(
            TestResult(
                categoryOrLevel = categoryOrLevel,
                score = score,
                totalQuestions = total,
                testType = testType
            )
        )
    }

    suspend fun resetAllProgress() {
        wordDao.resetAllWordStatus()
        testResultDao.clearAllResults()
        preferencesManager.resetPreferences()
        // Ensure database has words if empty
        if (wordDao.getWordCount() == 0) {
            wordDao.insertWords(SeedData.getInitialWords())
        }
    }

    fun speakWord(text: String) {
        ttsManager.speak(text)
    }

    fun updateSpeechRate(rate: Float) {
        preferencesManager.setSpeechRate(rate)
        ttsManager.setSpeechRate(rate)
    }
}
