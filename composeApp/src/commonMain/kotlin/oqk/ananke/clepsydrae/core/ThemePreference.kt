package oqk.ananke.clepsydrae.core

import androidx.compose.runtime.*

object ThemePreference {
    private var _isDarkTheme = mutableStateOf(true)
    val isDarkTheme: State<Boolean> = _isDarkTheme
    
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
    
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }
}
