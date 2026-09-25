package com.staymate.uptm.settings

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.core.content.edit

// File: settings/ThemeSettings.kt
// Dark mode preference management

object ThemeSettings {
    private const val PREFS_NAME = "staymate_prefs"
    private const val KEY_DARK_MODE = "dark_mode_enabled"

    fun saveDarkModeSetting(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_DARK_MODE, enabled) }
    }

    @Composable
    fun getDarkModeSetting(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, isSystemInDarkTheme())
    }
}