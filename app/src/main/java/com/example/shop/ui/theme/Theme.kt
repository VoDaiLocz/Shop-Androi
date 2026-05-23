package com.example.shop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ShopColors.SurfaceSoft,
    secondary = ShopColors.Wood,
    tertiary = ShopColors.Surface,
    background = ShopColors.WoodDark,
    surface = ShopColors.WoodDark,
    onPrimary = ShopColors.WoodDark,
    onSecondary = ShopColors.Surface,
    onTertiary = ShopColors.WoodDark,
    onBackground = ShopColors.Surface,
    onSurface = ShopColors.Surface
)

private val LightColorScheme = lightColorScheme(
    primary = ShopColors.WoodDark,
    secondary = ShopColors.Wood,
    tertiary = ShopColors.SurfaceSoft,
    background = ShopColors.Background,
    surface = ShopColors.Surface,
    surfaceVariant = ShopColors.SurfaceSoft,
    error = ShopColors.Error,
    onPrimary = ShopColors.Surface,
    onSecondary = ShopColors.Surface,
    onTertiary = ShopColors.WoodDark,
    onBackground = ShopColors.TextPrimary,
    onSurface = ShopColors.TextPrimary,
    onSurfaceVariant = ShopColors.TextSecondary
)

@Composable
fun ShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
