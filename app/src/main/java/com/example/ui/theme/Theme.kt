package com.example.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = ForestGreenDark,
    secondary = SageGreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE8DF),
    onSecondaryContainer = ForestGreenDark,
    tertiary = WarmAmberTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFECCF),
    onTertiaryContainer = WarmAmberDark,
    background = WarmCreamBackground,
    onBackground = Color(0xFF1B231F),
    surface = SoftCardSurface,
    onSurface = Color(0xFF1B231F),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF435147),
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = SageGreenLight,
    onPrimary = DarkForestBackground,
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = Color(0xFFC3E0CE),
    secondary = SageGreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Color(0xFFD0E3D7),
    tertiary = WarmAmberLight,
    onTertiary = DarkForestBackground,
    tertiaryContainer = WarmAmberDark,
    onTertiaryContainer = Color(0xFFFFE0B2),
    background = DarkForestBackground,
    onBackground = Color(0xFFE4EDE7),
    surface = DarkForestSurface,
    onSurface = Color(0xFFE4EDE7),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA5B8AD),
    outline = DarkOutline
)

@Composable
fun EvergreenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // false to preserve Evergreen's signature organic identity
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
