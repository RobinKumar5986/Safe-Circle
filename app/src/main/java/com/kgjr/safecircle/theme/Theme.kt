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
val SafeCircleSecondary = BaseColor.copy(alpha = 0.85f)
val SafeCircleTertiary = BaseColor.copy(alpha = 0.65f)

private val LightColorScheme = lightColorScheme(
    primary = SafeCirclePrimary,
    onPrimary = Color.White,
    secondary = SafeCircleSecondary,
    onSecondary = Color.White,
    tertiary = SafeCircleTertiary,
    onTertiary = Color.Black,

    background = Color(0xFFF8F8F8),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,

    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF757575),
)

private val DarkColorScheme = darkColorScheme(
    primary = SafeCirclePrimary,
    onPrimary = Color.Black,
    secondary = SafeCircleSecondary,
    onSecondary = Color.Black,
    tertiary = SafeCircleTertiary,
    onTertiary = Color.White,

    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,

    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
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
