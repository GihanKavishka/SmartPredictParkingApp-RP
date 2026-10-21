package com.example.androidsmartpredictparkinggihan.ui.theme

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
    primary = ParkingPrimary,
    onPrimary = ParkingOnPrimary,
    secondary = ParkingSecondary,
    tertiary = ParkingAccent,
    background = ParkingBackground,
    surface = ParkingSurface,
    error = ParkingError,
)

private val DarkColorScheme = darkColorScheme(
    primary = ParkingSecondary,
    onPrimary = ParkingPrimaryDark,
    secondary = ParkingAccent,
    tertiary = ParkingPrimary,
)

@Composable
fun AndroidSmartPredictParkingGihanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        content = content,
    )
}
