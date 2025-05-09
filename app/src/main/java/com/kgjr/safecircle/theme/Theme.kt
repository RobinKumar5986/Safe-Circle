package com.kgjr.safecircle.theme
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

// === Global Color Definitions ===
val BaseColor = Color(0xFFB57BFF)

val SafeCirclePrimary = BaseColor
val SafeCircleSecondary = BaseColor.copy(alpha = 0.8f)
val SafeCircleTertiary = BaseColor.copy(alpha = 0.6f)

// === Light Color Scheme ===
private val LightColorScheme = lightColorScheme(
    primary = SafeCirclePrimary,
    secondary = SafeCircleSecondary,
    tertiary = SafeCircleTertiary,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// === Dark Color Scheme ===
private val DarkColorScheme = darkColorScheme(
    primary = SafeCirclePrimary,
    secondary = SafeCircleSecondary,
    tertiary = SafeCircleTertiary,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

// === Theme Wrapper ===
@Composable
fun SafeCircleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Set Status Bar color
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    window?.let {
        val controller = WindowInsetsControllerCompat(it, it.decorView)
        controller.isAppearanceLightStatusBars = darkTheme
        it.statusBarColor = BaseColor.toArgb()
    }

    // Apply the theme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
