package com.trevor.final_resort.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Black,
    primaryContainer = GoldDark,
    onPrimaryContainer = White,
    secondary = GoldLight,
    onSecondary = Black,
    secondaryContainer = GoldAccent,
    onSecondaryContainer = Black,
    tertiary = GoldAccent,
    onTertiary = Black,
    background = Black,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    surfaceVariant = MediumGray,
    onSurfaceVariant = OffWhite,
    outline = Gold,
    outlineVariant = LightGray
)

private val LightColorScheme = lightColorScheme(
    primary = Gold,
    onPrimary = Black,
    primaryContainer = GoldLight,
    onPrimaryContainer = Black,
    secondary = GoldDark,
    onSecondary = White,
    secondaryContainer = GoldAccent,
    onSecondaryContainer = Black,
    tertiary = GoldAccent,
    onTertiary = Black,
    background = White,
    onBackground = Black,
    surface = OffWhite,
    onSurface = Black,
    surfaceVariant = Cream,
    onSurfaceVariant = Black,
    outline = Gold,
    outlineVariant = LightGray
)

@Composable
fun Final_resortTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our custom theme
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}