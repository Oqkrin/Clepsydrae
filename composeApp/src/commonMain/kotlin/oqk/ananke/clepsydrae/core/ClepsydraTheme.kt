package oqk.ananke.clepsydrae.core

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- DARK RAIN (Temporale Notturno) ---
val NightRainPrimary = Color(0xFF81D4FA)   // Blu acqua chiaro (riflessi)
val NightRainSecondary = Color(0xFF455A64) // Grigio ardesia scuro
val NightRainBackground = Color(0xFF101418) // Quasi nero, tono freddo
val NightRainSurface = Color(0xFF1C2227)    // Grigio bluastro

// --- LIGHT RAIN (Mattina Nebbiosa) ---
val LightRainPrimary = Color(0xFF0277BD)   // Blu oceano profondo
val LightRainSecondary = Color(0xFF90A4AE) // Grigio azzurro tenue
val LightRainBackground = Color(0xFFF0F4F8) // Bianco ghiaccio/nebbia
val LightRainSurface = Color(0xFFE1E8EE)

// Colori Timer (Active/Inactive)
val TimerActive = Color(0xFF00B0FF)
val TimerInactive = Color(0xFFB0BEC5)

val DarkRainScheme = darkColorScheme(
    primary = NightRainPrimary,
    onPrimary = Color(0xFF003546),
    primaryContainer = Color(0xFF004D64),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = NightRainSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF37474F),
    onSecondaryContainer = Color(0xFFCFD8DC),
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF003D36),
    tertiaryContainer = Color(0xFF00574E),
    onTertiaryContainer = Color(0xFFB2DFDB),
    error = Color(0xFFCF6679),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = NightRainBackground,
    onBackground = Color(0xFFE1E3E5),
    surface = NightRainSurface,
    onSurface = Color(0xFFE1E3E5),
    surfaceVariant = Color(0xFF263238),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF546E7A),
    outlineVariant = Color(0xFF37474F),
    scrim = Color.Black,
    inverseSurface = Color(0xFFE1E3E5),
    inverseOnSurface = Color(0xFF1C2227),
    inversePrimary = Color(0xFF006A88),
    surfaceTint = NightRainPrimary
)

val LightRainScheme = lightColorScheme(
    primary = LightRainPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D35),
    secondary = LightRainSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFD8DC),
    onSecondaryContainer = Color(0xFF263238),
    tertiary = Color(0xFF00897B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF003D36),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = LightRainBackground,
    onBackground = Color(0xFF191C1E),
    surface = LightRainSurface,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE),
    scrim = Color.Black,
    inverseSurface = Color(0xFF2E3133),
    inverseOnSurface = Color(0xFFF0F0F3),
    inversePrimary = Color(0xFF81D4FA),
    surfaceTint = LightRainPrimary
)

// Backward compatibility
val ClepsydraActiveColor = TimerActive
val ClepsydraInactiveColor = TimerInactive
val ClepsydraBackground = NightRainBackground
val ClepsydraColorScheme = DarkRainScheme
