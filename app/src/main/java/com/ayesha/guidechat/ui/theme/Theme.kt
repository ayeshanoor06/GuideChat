package com.ayesha.guidechat.ui.theme
import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DeepGreen,
    onPrimary = White,

    secondary = SoftGreen,
    onSecondary = DarkGreen,

    tertiary = LightMint,
    onTertiary = DarkGreen,

    background = OffWhite,
    onBackground = DarkGreen,

    surface = White,
    onSurface = DarkGreen,

    surfaceVariant = LightMint,
    onSurfaceVariant = DarkGreen,

    error = ErrorRed,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftGreen,
    onPrimary = DarkGreen,

    secondary = DeepGreen,
    onSecondary = White,

    tertiary = LightMint,
    onTertiary = DarkGreen,

    background = DarkGreen,
    onBackground = White,

    surface = Color(0xFF1B2B20),
    onSurface = White,

    surfaceVariant = DeepGreen,
    onSurfaceVariant = LightMint,

    error = ErrorRed,
    onError = White
)

@Composable
fun GuideChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
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