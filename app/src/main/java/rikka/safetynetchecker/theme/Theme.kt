package rikka.safetynetchecker.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource

@Composable
private fun darkColorPalette() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    darkColors(
        primary = colorResource(android.R.color.system_accent1_100),
        primaryVariant = colorResource(android.R.color.system_accent1_200),
        secondary = colorResource(android.R.color.system_accent1_300),
        secondaryVariant = colorResource(android.R.color.system_accent1_300),
        surface = colorResource(android.R.color.system_neutral1_900),
        background = colorResource(android.R.color.system_neutral1_900),
    )
} else {
    darkColors(
        primary = Purple200,
        primaryVariant = Purple700,
        secondary = Teal200,
        secondaryVariant = Teal200
    )
}

@Composable
private fun lightColorPalette() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    lightColors(
        primary = colorResource(android.R.color.system_accent1_500),
        primaryVariant = colorResource(android.R.color.system_accent1_500),
        secondary = colorResource(android.R.color.system_accent1_600),
        secondaryVariant = colorResource(android.R.color.system_accent1_600),
        surface = colorResource(android.R.color.system_neutral1_50),
        background = colorResource(android.R.color.system_neutral1_50),
    )
} else {
    lightColors(
        primary = Purple500,
        primaryVariant = Purple700,
        secondary = Teal200,
        secondaryVariant = Teal200,
    )
}

@Composable
fun YetAnotherSafetyNetCheckerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColorPalette()
    } else {
        lightColorPalette()
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
