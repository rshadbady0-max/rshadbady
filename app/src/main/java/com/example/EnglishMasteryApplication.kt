package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.EnglishMasteryRepository
import com.example.data.util.TtsManager
import com.example.util.PreferencesManager

class EnglishMasteryApplication : Application() {

    lateinit var repository: EnglishMasteryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        val preferencesManager = PreferencesManager(this)
        preferencesManager.updateStreak()
        val ttsManager = TtsManager(this)
        ttsManager.setSpeechRate(preferencesManager.speechRate.value)

        repository = EnglishMasteryRepository(
            wordDao = database.wordDao(),
            testResultDao = database.testResultDao(),
            ttsManager = ttsManager,
            preferencesManager = preferencesManager
        )
    }
}
