package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import oqk.ananke.clepsydrae.clepsydrae.domain.end
import oqk.ananke.clepsydrae.clepsydrae.domain.invertiDiatesi
import oqk.ananke.clepsydrae.clepsydrae.domain.shouldNotifyPomodoro
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
    
    private fun formatDate(date: LocalDate): String {
        val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$dayName ${date.day} $monthName ${date.year}"
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
    fun onAction(action: ClepsydraAction) {
        when (action) {
            is ClepsydraAction.OnSimpleCreate -> {
                _state.update {
                    it.copy(currentClepsydra = Clepsydra(lastStateChange = TimeSource.Monotonic.markNow()-4.minutes-50.seconds))
                }
            }

            is ClepsydraAction.OnCreateWithName -> {
                _state.update { it.copy(showNameDialog = true) }
            }

            is ClepsydraAction.OnClose -> {
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

            is ClepsydraAction.ToggleDiatesi -> {
                _state.update {
                    it.copy(currentClepsydra = it.currentClepsydra?.invertiDiatesi())
                }
            }

            is ClepsydraAction.ToggleHistory -> {
                _state.update { it.copy(showHistory = !it.showHistory) }
            }

            is ClepsydraAction.OnSetName -> {
                _state.update { it.copy(currentClepsydra = it.currentClepsydra?.copy(name = action.newName)) }
            }

            is ClepsydraAction.OnConfirmName -> {
                _state.update { it.copy(showNameDialog = false) }
            }

            is ClepsydraAction.OnRestore -> {
                _state.update {
                    it.copy(
                        currentClepsydra = action.clepsydra.copy(lastStateChange = TimeSource.Monotonic.markNow()),
                        showHistory = false
                    )
                }
            }

            is ClepsydraAction.OnDelete -> {
                viewModelScope.launch {
                    repository.deleteClepsydra(action.id)
                    loadClepsydraeForDate()
                }
            }

            is ClepsydraAction.OnSetNote -> {
                _state.update { it.copy(currentClepsydra = it.currentClepsydra?.copy(note = action.newNote)) }
            }

            is ClepsydraAction.OnPreviousDay -> {
                val currentDate = _state.value.selectedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                _state.update { it.copy(selectedDate = currentDate.plus(DatePeriod(days = -1))) }
                loadClepsydraeForDate()
            }

            is ClepsydraAction.OnNextDay -> {
                val currentDate = _state.value.selectedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                _state.update { it.copy(selectedDate = currentDate.plus(DatePeriod(days = 1))) }
                loadClepsydraeForDate()
            }
        }
    }
}
