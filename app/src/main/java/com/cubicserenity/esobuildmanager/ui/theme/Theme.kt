package com.cubicserenity.esobuildmanager.ui.theme

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

private val EsoGold = Color(0xFFC8A96E)
private val EsoDark = Color(0xFF1A1A2E)

private val LightColors = lightColorScheme(
    primary = Color(0xFF7B5C2B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDB0),
    secondary = Color(0xFF5C6033),
    onSecondary = Color.White,
    surface = Color(0xFFFFF8F0),
)

private val DarkColors = darkColorScheme(
    primary = EsoGold,
    onPrimary = Color(0xFF3E2A00),
    primaryContainer = Color(0xFF5A3E00),
    secondary = Color(0xFFBECA89),
    surface = EsoDark,
)

@Composable
fun EsoBuildManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
