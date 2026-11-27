package app.mimo.browser.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import app.mimo.browser.preferences.DesignLanguage
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.MimoHaptics
import app.mimo.browser.ui.components.LocalBrowserIcons
import app.mimo.browser.ui.components.LocalMimoIcons
import app.mimo.browser.ui.components.SoftBrowserIcons
import app.mimo.browser.ui.components.SoftMimoIcons
import app.mimo.browser.ui.components.TechnicalBrowserIcons
import app.mimo.browser.ui.components.TechnicalMimoIcons

val LocalMimoDark = staticCompositionLocalOf { false }
val LocalDesignLanguage = staticCompositionLocalOf { DesignLanguage.EXPRESSIVE }
val LocalInterfaceBlur = staticCompositionLocalOf { true }

/** The dominant tint of the visible content, used for ambient background light. */
val LocalAmbientTint = staticCompositionLocalOf<Color?> { null }

val MimoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

val NothingShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp),
)

private fun androidx.compose.material3.ColorScheme.glassSurfaces(): androidx.compose.material3.ColorScheme = copy(
    surface = surface.copy(alpha = 0.72f),
    surfaceContainerLowest = surfaceContainerLowest.copy(alpha = 0.62f),
    surfaceContainerLow = surfaceContainerLow.copy(alpha = 0.70f),
    surfaceContainer = surfaceContainer.copy(alpha = 0.74f),
    surfaceContainerHigh = surfaceContainerHigh.copy(alpha = 0.78f),
    surfaceContainerHighest = surfaceContainerHighest.copy(alpha = 0.82f),
    surfaceVariant = surfaceVariant.copy(alpha = 0.68f),
)

@Composable
fun MimoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    hapticsEnabled: Boolean = true,
    designLanguage: DesignLanguage = DesignLanguage.EXPRESSIVE,
    interfaceBlur: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val haptics = remember(hapticFeedback, hapticsEnabled) {
        MimoHaptics(hapticFeedback, hapticsEnabled)
    }
    val baseScheme = when {
        designLanguage == DesignLanguage.NOTHING ->
            if (darkTheme) NothingDarkColorScheme else NothingLightColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> MimoDarkColorScheme
        else -> MimoLightColorScheme
    }
    val colorScheme = if (interfaceBlur) baseScheme.glassSurfaces() else baseScheme
    val typography = if (designLanguage == DesignLanguage.NOTHING) NothingTypography else MimoTypography
    val shapes = if (designLanguage == DesignLanguage.NOTHING) NothingShapes else MimoShapes
    val motion = if (designLanguage == DesignLanguage.NOTHING) {
        MotionScheme.standard()
    } else {
        MotionScheme.expressive()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(
        LocalMimoDark provides darkTheme,
        LocalMimoHaptics provides haptics,
        LocalDesignLanguage provides designLanguage,
        LocalInterfaceBlur provides interfaceBlur,
        LocalMimoIcons provides if (designLanguage == DesignLanguage.NOTHING) {
            TechnicalMimoIcons
        } else {
            SoftMimoIcons
        },
        LocalBrowserIcons provides if (designLanguage == DesignLanguage.NOTHING) {
            TechnicalBrowserIcons
        } else {
            SoftBrowserIcons
        },
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = motion,
            shapes = shapes,
            typography = typography,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides colorScheme.onBackground,
                LocalTextStyle provides typography.bodyLarge,
                content = content,
            )
        }
    }
}
