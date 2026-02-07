package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import oqk.ananke.clepsydrae.clepsydrae.domain.end
import oqk.ananke.clepsydrae.clepsydrae.domain.invertiDiatesi
import oqk.ananke.clepsydrae.clepsydrae.domain.shouldNotifyPomodoro
import oqk.ananke.clepsydrae.core.formatDate
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

class ClepsydraScreenViewModel(private val repository: ClepsydraRepository) : ViewModel() {
    private val _state = MutableStateFlow(ClepsydraScreenState())
    val state = _state.asStateFlow()
    
    init {
        loadClepsydraeForDate()
        startPomodoroCheck()
    }
    
    @OptIn(ExperimentalTime::class)
    private fun loadClepsydraeForDate() {
        viewModelScope.launch {
            val date = _state.value.selectedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
            val list = repository.getClepsydraeByDate(date)
            val past = list.filter { it.init.elapsedNow() >= Duration.ZERO }
            val future = list.filter { it.init.elapsedNow() < Duration.ZERO }
            _state.update { it.copy(
                pastClepsydrae = past, 
                futureClepsydrae = future, 
                selectedDate = date,
                dateText = formatDate(date)
            ) }
        }
    }
    
    private fun startPomodoroCheck() {
        viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                _state.value.currentClepsydra?.let { clepsydra ->
                    if (clepsydra.shouldNotifyPomodoro()) {
                        _state.update { it.copy(pomodoroNotifying = true) }
                        delay(5.seconds)
                        _state.update { it.copy(pomodoroNotifying = false) }
                    }
                }
            }
        }
    }
    
    @OptIn(ExperimentalTime::class)
    fun onAction(action: ClepsydraScreenAction) {
        when (action) {
            is ClepsydraScreenAction.OnSimpleCreate -> {
                _state.update {
                    it.copy(currentClepsydra = Clepsydra(lastStateChange = TimeSource.Monotonic.markNow()-4.minutes-50.seconds))
                }
            }

            is ClepsydraScreenAction.OnCreateWithName -> {
                _state.update { it.copy(showNameDialog = true) }
            }

            is ClepsydraScreenAction.OnClose -> {
                _state.update { it.copy(currentClepsydra = it.currentClepsydra?.end() ) }

                viewModelScope.launch {
                    if (state.value.currentClepsydra!!.id == null) {
                        repository.insertClepsydra(state.value.currentClepsydra!!)
                    } else {
                        repository.updateClepsydra(state.value.currentClepsydra!!)
                    }
                    loadClepsydraeForDate()
                }

                _state.update { it.copy(currentClepsydra = null) }
            }

            is ClepsydraScreenAction.ToggleDiatesi -> {
                _state.update {
                    it.copy(currentClepsydra = it.currentClepsydra?.invertiDiatesi())
                }
            }

            is ClepsydraScreenAction.ToggleHistory -> {
                _state.update { it.copy(showHistory = !it.showHistory) }
            }

            is ClepsydraScreenAction.OnSetName -> {
                _state.update { it.copy(currentClepsydra = it.currentClepsydra?.copy(name = action.newName)) }
            }

            is ClepsydraScreenAction.OnConfirmName -> {
                _state.update { it.copy(showNameDialog = false) }
            }

            is ClepsydraScreenAction.OnRestore -> {
                _state.update {
                    it.copy(
                        currentClepsydra = action.clepsydra.copy(lastStateChange = TimeSource.Monotonic.markNow()),
                        showHistory = false
                    )
                }
            }

            is ClepsydraScreenAction.OnDelete -> {
                viewModelScope.launch {
                    repository.deleteClepsydra(action.id)
                    loadClepsydraeForDate()
                }
            }

            is ClepsydraScreenAction.OnSetNote -> {
                _state.update { it.copy(currentClepsydra = it.currentClepsydra?.copy(note = action.newNote)) }
            }

            is ClepsydraScreenAction.OnPreviousDay -> {
                val currentDate = _state.value.selectedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                _state.update { it.copy(selectedDate = currentDate.plus(DatePeriod(days = -1))) }
                loadClepsydraeForDate()
            }

            is ClepsydraScreenAction.OnNextDay -> {
                val currentDate = _state.value.selectedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                _state.update { it.copy(selectedDate = currentDate.plus(DatePeriod(days = 1))) }
                loadClepsydraeForDate()
            }
        }
    }
}
