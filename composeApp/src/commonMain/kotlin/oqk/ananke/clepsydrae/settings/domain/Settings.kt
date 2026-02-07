package oqk.ananke.clepsydrae.settings.domain

data class Settings(
    val isDarkTheme: Boolean = true,
    val theme: String = "rain",
    val fontSize: Int = 14,
    val uiScale: Float = 1.0f
)
