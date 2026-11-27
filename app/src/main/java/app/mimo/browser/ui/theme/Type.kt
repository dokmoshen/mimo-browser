package app.mimo.browser.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.mimo.browser.R

private fun manrope(weight: Int) = Font(
    resId = R.font.manrope_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun unbounded(weight: Int) = Font(
    resId = R.font.unbounded_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun handjet(weight: Int) = Font(
    resId = R.font.handjet_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Handjet = FontFamily(
    handjet(400),
    handjet(500),
    handjet(700),
    handjet(900),
)

val Manrope = FontFamily(
    manrope(400),
    manrope(500),
    manrope(600),
    manrope(700),
    manrope(800),
)

val Unbounded = FontFamily(
    unbounded(400),
    unbounded(500),
    unbounded(600),
    unbounded(700),
)

val MimoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 46.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.0).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.6).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 29.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.Medium,
        fontSize = 25.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 29.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp,
    ),
)


val NothingTypography = Typography(
    displayLarge = MimoTypography.displayLarge.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
    ),
    displayMedium = MimoTypography.displayMedium.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
    ),
    displaySmall = MimoTypography.displaySmall.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
    ),
    headlineLarge = MimoTypography.headlineLarge.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
    ),
    headlineMedium = MimoTypography.headlineMedium.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp,
    ),
    headlineSmall = MimoTypography.headlineSmall.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp,
    ),
    titleLarge = MimoTypography.titleLarge.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp,
    ),
    titleMedium = MimoTypography.titleMedium.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.3.sp,
    ),
    titleSmall = MimoTypography.titleSmall.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.2.sp,
    ),
    bodyLarge = MimoTypography.bodyLarge.copy(
        fontFamily = Handjet,
        fontSize = 17.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.2.sp,
    ),
    bodyMedium = MimoTypography.bodyMedium.copy(
        fontFamily = Handjet,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp,
    ),
    bodySmall = MimoTypography.bodySmall.copy(
        fontFamily = Handjet,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
    ),
    labelLarge = MimoTypography.labelLarge.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.3.sp,
    ),
    labelMedium = MimoTypography.labelMedium.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.3.sp,
    ),
    labelSmall = MimoTypography.labelSmall.copy(
        fontFamily = Handjet,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.3.sp,
    ),
)
