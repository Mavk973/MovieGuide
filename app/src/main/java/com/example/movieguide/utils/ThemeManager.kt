package com.example.movieguide.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.movieguide.R

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "selected_theme"
    
    // Значения темы
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"
    
    fun getSupportedThemes(context: Context): Map<String, String> {
        return mapOf(
            THEME_LIGHT to context.getString(R.string.theme_light),
            THEME_DARK to context.getString(R.string.theme_dark),
            THEME_SYSTEM to context.getString(R.string.theme_system)
        )
    }
    
    fun getCurrentTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    fun setTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme).apply()
    }
    
    fun isDarkTheme(context: Context, isSystemInDarkTheme: Boolean): Boolean {
        return when (getCurrentTheme(context)) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_SYSTEM -> isSystemInDarkTheme
            else -> isSystemInDarkTheme
        }
    }
}

