package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val englishWord: String,
    val arabicTranslation: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val level: String, // "A1-A2", "B1-B2", "C1-C2"
    val category: String, // "Family", "Food", "Work", "Travel", "Emotions", "Technology", etc.
    val isLearned: Boolean = false,
    val isMastered: Boolean = false,
    val lastReviewed: Long = System.currentTimeMillis()
)
