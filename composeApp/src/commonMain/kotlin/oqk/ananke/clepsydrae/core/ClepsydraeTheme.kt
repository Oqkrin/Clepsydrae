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

// ============================================================================
// MIDNIGHT MIST (Dark Rain Aesthetic)
// Deep oceanic backgrounds with bioluminescent cyan and frosted slate.
// ============================================================================
private val MidnightPrimary = Color(0xFF82CFFF)
private val MidnightOnPrimary = Color(0xFF00344B)
private val MidnightPrimaryContainer = Color(0xFF004C6A)
private val MidnightOnPrimaryContainer = Color(0xFFC3E8FF)

private val MidnightSecondary = Color(0xFFA5C8D4)
private val MidnightOnSecondary = Color(0xFF0A313E)
private val MidnightSecondaryContainer = Color(0xFF264855)
private val MidnightOnSecondaryContainer = Color(0xFFC1E4F1)

private val MidnightTertiary = Color(0xFF5EEAD4)
private val MidnightOnTertiary = Color(0xFF003730)
private val MidnightTertiaryContainer = Color(0xFF005047)
private val MidnightOnTertiaryContainer = Color(0xFF7FF8E2)

private val MidnightBackground = Color(0xFF0A1015)
private val MidnightOnBackground = Color(0xFFDEE3E6)
private val MidnightSurface = Color(0xFF0E151B)
private val MidnightOnSurface = Color(0xFFDEE3E6)

private val MidnightSurfaceVariant = Color(0xFF212E36)
private val MidnightOnSurfaceVariant = Color(0xFFBEC8CE)
private val MidnightOutline = Color(0xFF889298)

// ============================================================================
// MORNING DEW (Light Rain Aesthetic)
// Crisp morning skies, soft fog grays, and deep ocean primary text.
// ============================================================================
private val MorningPrimary = Color(0xFF00658D)
private val MorningOnPrimary = Color(0xFFFFFFFF)
private val MorningPrimaryContainer = Color(0xFFC3E8FF)
private val MorningOnPrimaryContainer = Color(0xFF001E2D)

private val MorningSecondary = Color(0xFF40606D)
private val MorningOnSecondary = Color(0xFFFFFFFF)
private val MorningSecondaryContainer = Color(0xFFC1E4F1)
private val MorningOnSecondaryContainer = Color(0xFF001F29)

private val MorningTertiary = Color(0xFF006A5F)
private val MorningOnTertiary = Color(0xFFFFFFFF)
private val MorningTertiaryContainer = Color(0xFF7FF8E2)
private val MorningOnTertiaryContainer = Color(0xFF00201C)

private val MorningBackground = Color(0xFFF6FAFD)
private val MorningOnBackground = Color(0xFF181C1E)
private val MorningSurface = Color(0xFFF9FBFA)
private val MorningOnSurface = Color(0xFF181C1E)

private val MorningSurfaceVariant = Color(0xFFDBE4E9)
private val MorningOnSurfaceVariant = Color(0xFF40484C)
private val MorningOutline = Color(0xFF70787D)

// ============================================================================
// SCHEMES
// ============================================================================
val DarkRainScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = MidnightOnPrimary,
    primaryContainer = MidnightPrimaryContainer,
    onPrimaryContainer = MidnightOnPrimaryContainer,
    secondary = MidnightSecondary,
    onSecondary = MidnightOnSecondary,
    secondaryContainer = MidnightSecondaryContainer,
    onSecondaryContainer = MidnightOnSecondaryContainer,
    tertiary = MidnightTertiary,
    onTertiary = MidnightOnTertiary,
    tertiaryContainer = MidnightTertiaryContainer,
    onTertiaryContainer = MidnightOnTertiaryContainer,
    background = MidnightBackground,
    onBackground = MidnightOnBackground,
    surface = MidnightSurface,
    onSurface = MidnightOnSurface,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MidnightOnSurfaceVariant,
    outline = MidnightOutline,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val LightRainScheme = lightColorScheme(
    primary = MorningPrimary,
    onPrimary = MorningOnPrimary,
    primaryContainer = MorningPrimaryContainer,
    onPrimaryContainer = MorningOnPrimaryContainer,
    secondary = MorningSecondary,
    onSecondary = MorningOnSecondary,
    secondaryContainer = MorningSecondaryContainer,
    onSecondaryContainer = MorningOnSecondaryContainer,
    tertiary = MorningTertiary,
    onTertiary = MorningOnTertiary,
    tertiaryContainer = MorningTertiaryContainer,
    onTertiaryContainer = MorningOnTertiaryContainer,
    background = MorningBackground,
    onBackground = MorningOnBackground,
    surface = MorningSurface,
    onSurface = MorningOnSurface,
    surfaceVariant = MorningSurfaceVariant,
    onSurfaceVariant = MorningOnSurfaceVariant,
    outline = MorningOutline,
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
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

