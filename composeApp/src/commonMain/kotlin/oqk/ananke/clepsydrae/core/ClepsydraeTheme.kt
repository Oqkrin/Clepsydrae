package oqk.ananke.clepsydrae.core

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

// PRIMARY — bioluminescent rain light
private val AbyssPrimary = Color(0xFF6FD3FF)
private val AbyssOnPrimary = Color(0xFF002533)
private val AbyssPrimaryContainer = Color(0xFF003A52)
private val AbyssOnPrimaryContainer = Color(0xFFCBEFFF)

// SECONDARY — fog steel
private val AbyssSecondary = Color(0xFF9FBFCC)
private val AbyssOnSecondary = Color(0xFF08303C)
private val AbyssSecondaryContainer = Color(0xFF213F4B)
private val AbyssOnSecondaryContainer = Color(0xFFCBE5F0)

// TERTIARY — rainfall energy accent
private val AbyssTertiary = Color(0xFF7CE0C6)
private val AbyssOnTertiary = Color(0xFF00382E)
private val AbyssTertiaryContainer = Color(0xFF005046)
private val AbyssOnTertiaryContainer = Color(0xFFA7F5E2)

// TRUE MATERIAL SURFACE STACK
private val AbyssBackground = Color(0xFF070C10)
private val AbyssSurface = Color(0xFF0D1419)
private val AbyssSurfaceVariant = Color(0xFF26333B)

private val AbyssOnBackground = Color(0xFFDDE5EA)
private val AbyssOnSurface = Color(0xFFDDE5EA)
private val AbyssOnSurfaceVariant = Color(0xFFBAC6CC)

private val AbyssOutline = Color(0xFF7A878E)


//LIGHT THEME COLORS
private val DewPrimary = Color(0xFF005E82)
private val DewOnPrimary = Color.White
private val DewPrimaryContainer = Color(0xFFBFE9FF)
private val DewOnPrimaryContainer = Color(0xFF001E2A)

private val DewSecondary = Color(0xFF4A626C)
private val DewOnSecondary = Color.White
private val DewSecondaryContainer = Color(0xFFCDE7F0)
private val DewOnSecondaryContainer = Color(0xFF051F27)

private val DewTertiary = Color(0xFF006B5D)
private val DewOnTertiary = Color.White
private val DewTertiaryContainer = Color(0xFF97F3E0)
private val DewOnTertiaryContainer = Color(0xFF00201B)

// Wet reflective surfaces
private val DewBackground = Color(0xFFF2F7FA)
private val DewSurface = Color(0xFFF7FBFD)
private val DewSurfaceVariant = Color(0xFFD6E3E9)

private val DewOnBackground = Color(0xFF161C1F)
private val DewOnSurface = Color(0xFF161C1F)
private val DewOnSurfaceVariant = Color(0xFF3F4A50)

private val DewOutline = Color(0xFF6F7A80)

// ============================================================================
// SCHEMES
// ============================================================================
val DarkRainScheme = darkColorScheme(
    primary = AbyssPrimary,
    onPrimary = AbyssOnPrimary,
    primaryContainer = AbyssPrimaryContainer,
    onPrimaryContainer = AbyssOnPrimaryContainer,

    secondary = AbyssSecondary,
    onSecondary = AbyssOnSecondary,
    secondaryContainer = AbyssSecondaryContainer,
    onSecondaryContainer = AbyssOnSecondaryContainer,

    tertiary = AbyssTertiary,
    onTertiary = AbyssOnTertiary,
    tertiaryContainer = AbyssTertiaryContainer,
    onTertiaryContainer = AbyssOnTertiaryContainer,

    background = AbyssBackground,
    surface = AbyssSurface,
    surfaceVariant = AbyssSurfaceVariant,

    onBackground = AbyssOnBackground,
    onSurface = AbyssOnSurface,
    onSurfaceVariant = AbyssOnSurfaceVariant,

    outline = AbyssOutline
)
val LightRainScheme = lightColorScheme(
    primary = DewPrimary,
    onPrimary = DewOnPrimary,
    primaryContainer = DewPrimaryContainer,
    onPrimaryContainer = DewOnPrimaryContainer,

    secondary = DewSecondary,
    onSecondary = DewOnSecondary,
    secondaryContainer = DewSecondaryContainer,
    onSecondaryContainer = DewOnSecondaryContainer,

    tertiary = DewTertiary,
    onTertiary = DewOnTertiary,
    tertiaryContainer = DewTertiaryContainer,
    onTertiaryContainer = DewOnTertiaryContainer,

    background = DewBackground,
    surface = DewSurface,
    surfaceVariant = DewSurfaceVariant,

    onBackground = DewOnBackground,
    onSurface = DewOnSurface,
    onSurfaceVariant = DewOnSurfaceVariant,

    outline = DewOutline
)



fun clepsydraTypography(fs: Int) = Typography(
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = fs*22.sp, lineHeight = fs*28.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = fs*16.sp, lineHeight = fs*24.sp, letterSpacing = 0.15.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = fs*16.sp, lineHeight = fs*24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = fs*14.sp, lineHeight = fs*20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = fs*12.sp, lineHeight = fs*16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = fs*14.sp, lineHeight = fs*20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = fs*12.sp, lineHeight = fs*16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = fs*11.sp, lineHeight = fs*16.sp, letterSpacing = 0.5.sp)
)

@Composable
fun ClepsydraeTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String = "rain",
    fontSize: Int = 1,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "dynamic" -> dynamicColorScheme(isDarkTheme) ?: if (isDarkTheme) DarkRainScheme else LightRainScheme
        else -> if (isDarkTheme) DarkRainScheme else LightRainScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = clepsydraTypography(fs = fontSize),
        content = content
    )
}

