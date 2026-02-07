package oqk.ananke.clepsydrae.settings.presentation

import oqk.ananke.clepsydrae.settings.domain.Settings

data class SettingsScreenState(
    val settings: Settings = Settings()
)
