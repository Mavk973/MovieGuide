package com.example.movieguide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
    
    private var googleSignInCallback: ((String?) -> Unit)? = null
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                googleSignInCallback?.invoke(account?.idToken)
            } catch (e: ApiException) {
                googleSignInCallback?.invoke(null)
            }
        } else {
            googleSignInCallback?.invoke(null)
        }
    }
    
    override fun attachBaseContext(newBase: Context?) {
        try {
            Log.d(TAG, "attachBaseContext: начало")
            super.attachBaseContext(
                newBase?.let { LanguageManager.getLocalizedContext(it) }
            )
            Log.d(TAG, "attachBaseContext: успешно")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка в attachBaseContext", e)
            super.attachBaseContext(newBase)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Log.d(TAG, "onCreate: начало")
            super.onCreate(savedInstanceState)
            // Язык уже применен в attachBaseContext, не нужно вызывать updateResources здесь
            enableEdgeToEdge()
            Log.d(TAG, "onCreate: enableEdgeToEdge выполнено")
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
                    },
                    onGoogleSignInRequest = { intent, callback ->
                        googleSignInCallback = callback
                        googleSignInLauncher.launch(intent)
                    }
                )
            }
        }
        } catch (e: Exception) {
            Log.e(TAG, "КРИТИЧЕСКАЯ ОШИБКА в onCreate", e)
            throw e
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun LanguageAwareContent(
    onThemeChanged: ((String) -> Unit)? = null,
    onGoogleSignInRequest: ((Intent, (String?) -> Unit) -> Unit)? = null
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
        onThemeChanged = onThemeChanged,
        onGoogleSignInRequest = onGoogleSignInRequest
    )
}