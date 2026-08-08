package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY id ASC")
    fun getAllWords(): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE level = :level ORDER BY id ASC")
    fun getWordsByLevel(level: String): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE category = :category ORDER BY id ASC")
    fun getWordsByCategory(category: String): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE isLearned = 1 ORDER BY lastReviewed DESC")
    fun getLearnedWords(): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE isMastered = 1 ORDER BY lastReviewed DESC")
    fun getMasteredWords(): Flow<List<Word>>

    @Query("SELECT count(*) FROM words")
    suspend fun getWordCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Update
    suspend fun updateWord(word: Word)

    @Query("UPDATE words SET isLearned = :isLearned, isMastered = :isMastered, lastReviewed = :timestamp WHERE id = :id")
    suspend fun updateWordStatus(id: Int, isLearned: Boolean, isMastered: Boolean, timestamp: Long)

    @Query("UPDATE words SET isLearned = 0, isMastered = 0")
    suspend fun resetAllWordStatus()
}
