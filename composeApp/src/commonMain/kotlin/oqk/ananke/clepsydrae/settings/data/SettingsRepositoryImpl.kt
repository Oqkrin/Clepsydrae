package oqk.ananke.clepsydrae.settings.data

import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import oqk.ananke.clepsydrae.Database
import oqk.ananke.clepsydrae.clepsydrae.data.toBool
import oqk.ananke.clepsydrae.clepsydrae.data.toBooleanLong
import oqk.ananke.clepsydrae.settings.domain.Settings
import oqk.ananke.clepsydrae.settings.domain.SettingsRepository

class SettingsRepositoryImpl(private val database: Database) : SettingsRepository {
    private val _settings = MutableStateFlow(loadSettings())
    
    override fun getSettings(): Flow<Settings> = _settings.asStateFlow()
    
    private fun loadSettings(): Settings {
        val settings = database.settingsQueries.getSettings().executeAsOneOrNull()
        return Settings(
            isFirstClepsydra = settings?.isFirstClepsydra?.toBool() ?: true,
            isDarkTheme = settings?.isDarkTheme?.toBool() ?: false,
            theme = settings?.theme ?: "rain",
            fontSize = settings?.fontSize?.toInt() ?: 14,
            uiScale = settings?.uiScale?.toFloat() ?: 1.0f
        )
    }
    
    override suspend fun updateSettings(settings: Settings) {
        database.settingsQueries.upsertSettings(
            isDarkTheme = settings.isDarkTheme.toBooleanLong(),
            theme = settings.theme,
            fontSize = settings.fontSize.toLong(),
            uiScale = settings.uiScale.toDouble(),
            isFirstClepsydra = settings.isFirstClepsydra.toBooleanLong()
        )
        _settings.value = settings
    }
}
