package oqk.ananke.clepsydrae.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import oqk.ananke.clepsydrae.settings.domain.Settings
import oqk.ananke.clepsydrae.settings.domain.SettingsRepository

class SettingsScreenViewModel(private val repository: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state = _state.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getSettings().collect { settings ->
                _state.update { it.copy(settings = settings) }
            }
        }
    }
    
    fun onAction(action: SettingsAction) {
        viewModelScope.launch {
            val current = _state.value.settings
            when (action) {
                is SettingsAction.ToggleTheme -> {
                    repository.updateSettings(current.copy(isDarkTheme = !current.isDarkTheme))
                }
                is SettingsAction.SetTheme -> {
                    repository.updateSettings(current.copy(theme = action.themeName))
                }
                is SettingsAction.SetFontSize -> {
                    repository.updateSettings(current.copy(fontSize = action.size))
                }
                is SettingsAction.SetUIScale -> {
                    repository.updateSettings(current.copy(uiScale = action.scale))
                }
            }
        }
    }
}
