package oqk.ananke.clepsydrae.settings.presentation

sealed interface SettingsAction {
    data object ToggleTheme : SettingsAction
    data class SetTheme(val themeName: String) : SettingsAction
    data class SetFontSize(val size: Int) : SettingsAction
    data class SetUIScale(val scale: Float) : SettingsAction
}
