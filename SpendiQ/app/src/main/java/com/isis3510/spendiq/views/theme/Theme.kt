package com.isis3510.spendiq.views.theme

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


// Esquema de colores para el tema oscuro
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = White,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Black,
    surface = Gray90,
    onBackground = White,
    onSurface = White
)

// Esquema de colores para el tema claro
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Black,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = White,
    surface = Gray10,
    onBackground = Black,
    onSurface = Black
)


@Composable
fun SpendiQTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
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
