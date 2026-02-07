package oqk.ananke.clepsydrae.core

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun dynamicColorScheme(isDark: Boolean): ColorScheme?
