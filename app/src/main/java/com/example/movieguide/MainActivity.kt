package com.example.movieguide

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.movieguide.navigation.NavGraph
import com.example.movieguide.ui.theme.MovieGuideTheme
import com.example.movieguide.utils.LanguageManager
import com.example.movieguide.utils.ThemeManager

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.let { LanguageManager.getLocalizedContext(it) }
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply saved language
        LanguageManager.updateResources(this, LanguageManager.getCurrentLanguage(this))
        enableEdgeToEdge()
        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            var darkTheme by remember { 
                mutableStateOf(ThemeManager.isDarkTheme(this, isSystemInDarkTheme))
            }
            
            // Обновляем тему при изменении системной темы
            LaunchedEffect(isSystemInDarkTheme) {
                darkTheme = ThemeManager.isDarkTheme(this@MainActivity, isSystemInDarkTheme)
            }
            
            MovieGuideTheme(darkTheme = darkTheme) {
                LanguageAwareContent(
                    onThemeChanged = { newTheme ->
                        ThemeManager.setTheme(this@MainActivity, newTheme)
                        darkTheme = ThemeManager.isDarkTheme(this@MainActivity, isSystemInDarkTheme)
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageAwareContent(
    onThemeChanged: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    
    NavGraph(
        onLanguageChanged = { newLanguage ->
            try {
                // Сохраняем язык синхронно
                LanguageManager.setLanguage(context, newLanguage)
                // Пересоздаем Activity для немедленного применения языка
                (context as? MainActivity)?.recreate()
            } catch (e: Exception) {
                // Обрабатываем ошибку
                e.printStackTrace()
            }
        },
        onThemeChanged = onThemeChanged
    )
}