package com.example.movieguide.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PremiumDarkColorScheme = darkColorScheme(
    primary = MoviePurple,
    onPrimary = MovieTextPrimary,
    primaryContainer = MoviePurpleLight,
    onPrimaryContainer = MovieDark,
    
    secondary = MovieCyan,
    onSecondary = MovieDark,
    secondaryContainer = MovieCyan.copy(alpha = 0.2f),
    onSecondaryContainer = MovieCyan,
    
    tertiary = MovieGold,
    onTertiary = MovieDark,
    tertiaryContainer = MovieGoldLight.copy(alpha = 0.2f),
    onTertiaryContainer = MovieGold,
    
    error = MoviePink,
    onError = MovieTextPrimary,
    errorContainer = MoviePink.copy(alpha = 0.2f),
    onErrorContainer = MoviePink,
    
    background = MovieDark,
    onBackground = MovieTextPrimary,
    
    surface = MovieSurface,
    onSurface = MovieTextPrimary,
    surfaceVariant = MovieSurfaceVariant,
    onSurfaceVariant = MovieTextSecondary,
    
    outline = MovieTextTertiary,
    outlineVariant = MovieSurfaceVariant,
    
    scrim = MovieDark.copy(alpha = 0.8f),
    inverseSurface = MovieTextPrimary,
    inverseOnSurface = MovieDark,
    inversePrimary = MoviePurple
)

private val PremiumLightColorScheme = lightColorScheme(
    primary = MoviePurple,
    onPrimary = MovieTextPrimary,
    primaryContainer = MoviePurpleLight,
    onPrimaryContainer = MovieDark,
    
    secondary = MovieCyan,
    onSecondary = MovieTextPrimary,
    secondaryContainer = MovieCyan.copy(alpha = 0.2f),
    onSecondaryContainer = MovieCyan,
    
    tertiary = MovieGold,
    onTertiary = MovieDark,
    tertiaryContainer = MovieGoldLight.copy(alpha = 0.2f),
    onTertiaryContainer = MovieGold,
    
    error = MoviePink,
    onError = MovieTextPrimary,
    errorContainer = MoviePink.copy(alpha = 0.2f),
    onErrorContainer = MoviePink,
    
    background = MovieLight,
    onBackground = MovieDark,
    
    surface = MovieLight,
    onSurface = MovieDark,
    surfaceVariant = MovieLightSecondary,
    onSurfaceVariant = MovieTextTertiary,
    
    outline = MovieTextTertiary,
    outlineVariant = MovieLightSecondary,
    
    scrim = MovieDark.copy(alpha = 0.8f),
    inverseSurface = MovieDark,
    inverseOnSurface = MovieTextPrimary,
    inversePrimary = MoviePurple
)

@Composable
fun MovieGuideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our premium color scheme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PremiumDarkColorScheme
        else -> PremiumLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}