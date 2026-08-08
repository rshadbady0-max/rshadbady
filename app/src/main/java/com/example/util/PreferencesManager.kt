package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("english_mastery_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _speechRate = MutableStateFlow(prefs.getFloat(KEY_SPEECH_RATE, 1.0f))
    val speechRate: StateFlow<Float> = _speechRate

    private val _streakDays = MutableStateFlow(prefs.getInt(KEY_STREAK_DAYS, 1))
    val streakDays: StateFlow<Int> = _streakDays

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
        _speechRate.value = rate
    }

    fun updateStreak() {
        val lastLogin = prefs.getLong(KEY_LAST_LOGIN, 0L)
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        if (now - lastLogin > oneDayMs && now - lastLogin < 2 * oneDayMs) {
            val current = prefs.getInt(KEY_STREAK_DAYS, 1) + 1
            prefs.edit().putInt(KEY_STREAK_DAYS, current).putLong(KEY_LAST_LOGIN, now).apply()
            _streakDays.value = current
        } else if (now - lastLogin >= 2 * oneDayMs) {
            prefs.edit().putInt(KEY_STREAK_DAYS, 1).putLong(KEY_LAST_LOGIN, now).apply()
            _streakDays.value = 1
        } else if (lastLogin == 0L) {
            prefs.edit().putInt(KEY_STREAK_DAYS, 1).putLong(KEY_LAST_LOGIN, now).apply()
            _streakDays.value = 1
        }
    }

    fun resetPreferences() {
        prefs.edit().clear().apply()
        _isDarkMode.value = false
        _speechRate.value = 1.0f
        _streakDays.value = 1
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_STREAK_DAYS = "streak_days"
        private const val KEY_LAST_LOGIN = "last_login"
    }
}
