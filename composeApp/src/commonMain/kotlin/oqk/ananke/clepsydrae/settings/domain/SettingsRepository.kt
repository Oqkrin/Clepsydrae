package oqk.ananke.clepsydrae.settings.domain

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun updateSettings(settings: Settings)
}
