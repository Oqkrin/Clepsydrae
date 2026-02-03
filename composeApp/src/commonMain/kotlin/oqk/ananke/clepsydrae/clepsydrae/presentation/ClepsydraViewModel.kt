package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import oqk.ananke.clepsydrae.clepsydrae.domain.invertiDiatesi
import kotlin.time.Duration.Companion.seconds

class ClepsydraViewModel(private val repository: ClepsydraRepository) : ViewModel() {
    private val _state = MutableStateFlow(ClepsydraState())
    val state = _state.asStateFlow()
    
    init {
        startTimer()
        loadClepsydrae()
    }
    
    private fun loadClepsydrae() {
        viewModelScope.launch {
            val list = repository.getAllClepsydrae()
            _state.update { it.copy(savedClepsydrae = list) }
        }
    }
    
    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                _state.update { it.copy() }
            }
        }
    }
    
    fun handleAction(action: ClepsydraAction) {
        when (action) {
            is ClepsydraAction.Create -> {
                _state.update {
                    it.copy(currentClepsydra = Clepsydra(name = null))
                }
            }
            is ClepsydraAction.CreateWithName -> {
                _state.update { it.copy(showNameDialog = true) }
            }
            is ClepsydraAction.Close -> {
                _state.value.currentClepsydra?.let { clepsydra ->
                    val elapsed = clepsydra.lastStateChange.elapsedNow()
                    val finalClepsydra = if (clepsydra.isActive) {
                        clepsydra.copy(
                            totalActiveTime = clepsydra.totalActiveTime + elapsed,
                            isActive = false
                        )
                    } else {
                        clepsydra.copy(
                            totalPassiveTime = clepsydra.totalPassiveTime + elapsed
                        )
                    }
                    viewModelScope.launch {
                        if (finalClepsydra.id == null) {
                            repository.insertClepsydra(finalClepsydra)
                        } else {
                            repository.updateClepsydra(finalClepsydra)
                        }
                        loadClepsydrae()
                    }
                    _state.update { it.copy(currentClepsydra = null) }
                }
            }
            is ClepsydraAction.Toggle -> {
                _state.update {
                    it.copy(currentClepsydra = it.currentClepsydra?.invertiDiatesi())
                }
            }
            is ClepsydraAction.ToggleHistory -> {
                _state.update { it.copy(showHistory = !it.showHistory) }
            }
            is ClepsydraAction.SetName -> {
                _state.update { it.copy(name = action.newName) }
            }
            is ClepsydraAction.ConfirmName -> {
                _state.update {
                    it.copy(
                        currentClepsydra = it.currentClepsydra?.copy(name = it.name.takeIf { it.isNotBlank() })
                            ?: Clepsydra(name = it.name.takeIf { it.isNotBlank() }),
                        showNameDialog = false,
                        name = ""
                    )
                }
            }
            is ClepsydraAction.Restore -> {
                _state.update {
                    it.copy(
                        currentClepsydra = action.clepsydra.copy(lastStateChange = kotlin.time.TimeSource.Monotonic.markNow()),
                        showHistory = false
                    )
                }
            }
            is ClepsydraAction.Delete -> {
                viewModelScope.launch {
                    repository.deleteClepsydra(action.id)
                    loadClepsydrae()
                }
            }
        }
    }
}