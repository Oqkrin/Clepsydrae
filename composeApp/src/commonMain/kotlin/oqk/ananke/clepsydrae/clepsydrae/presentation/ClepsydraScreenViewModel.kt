package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import oqk.ananke.clepsydrae.clepsydrae.data.toTimeMark
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import oqk.ananke.clepsydrae.clepsydrae.domain.end
import oqk.ananke.clepsydrae.clepsydrae.domain.invertiDiatesi
import oqk.ananke.clepsydrae.core.NotificationManager
import oqk.ananke.clepsydrae.journal.domain.formatDate
import oqk.ananke.clepsydrae.journal.domain.JournalRepository
import oqk.ananke.clepsydrae.settings.domain.SettingsRepository
import kotlin.let
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

class ClepsydraScreenViewModel(
    private val clepsydraRepository: ClepsydraRepository,
    private val settingsRepository: SettingsRepository,
    private val journalRepository: JournalRepository,
    private val notificationManager: NotificationManager
    ) : ViewModel() {
    private val _state = MutableStateFlow(ClepsydraScreenState())
    val state = _state.asStateFlow()
    
    init {
        loadClepsydraeForDate()
    }
    
    @OptIn(ExperimentalTime::class)
    private fun loadClepsydraeForDate() {
        viewModelScope.launch {
            val date = _state.value.currentLocalDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
            val list = clepsydraRepository.getClepsydraeByDate(date)
            val past = list.filter { it.init.elapsedNow() >= Duration.ZERO }
            val future = list.filter { it.init.elapsedNow() < Duration.ZERO }
            _state.update { it.copy(
                pastClepsydrae = past, 
                futureClepsydrae = future, 
                currentLocalDate = date,
                dateText = formatDate(date),
                startOfDay = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds().toTimeMark()
            ) }
        }
    }
    
    @OptIn(ExperimentalTime::class)
    fun onAction(action: ClepsydraScreenAction) {
        when (action) {

            is ClepsydraScreenAction.OnFirstClepsydraCreation -> {
                _state.update { it.copy( showNotificationPermissionPopUp = true) }
            }

            is ClepsydraScreenAction.OnFirstClepsydraCreationOnResult -> {
                viewModelScope.launch {
                    settingsRepository.updateSettings(
                        settingsRepository.getSettings().first().copy(isFirstClepsydra = false)
                    )

                    _state.update { it.copy( showNotificationPermissionPopUp = false) }
                }
            }

            is ClepsydraScreenAction.OnCreateClepsydra -> {

                val cc: ClepsydraScreenAction.OnCreateClepsydra = action

                state.value.coreClepsydra?.let {
                    onAction(ClepsydraScreenAction.OnCloseCoreClepsydra)
                }

                _state.update {
                    val fin = listOfNotNull(cc.finHours, cc.finMinutes, cc.finSeconds)
                        .reduceOrNull { acc, d -> acc + d }
                        ?.takeIf { value -> value > Duration.ZERO }

                    val init = listOfNotNull(cc.initHours, cc.initMinutes, cc.initSeconds)
                        .reduceOrNull { acc, d -> acc + d }?.takeIf { value -> value >= Duration.ZERO }

                    it.copy(
                        coreClepsydra =
                        Clepsydra(
                            name = cc.name,
                            note = cc.note,
                            tags = cc.tags,
                            init = cc.init ?: (cc.now + (init ?: Duration.ZERO)),
                            pomodoroPassive = cc.passiveGoal,
                            pomodoroActive = cc.activeGoal,
                            fin = cc.fin ?: fin?.let { duration -> (cc.init ?: (cc.now + (init ?: Duration.ZERO))) + duration },
                        )
                    )
                }
            }

            is ClepsydraScreenAction.OnCloseCoreClepsydra -> {
                _state.update { it.copy(coreClepsydra = it.coreClepsydra?.end() ) }

                viewModelScope.launch {
                    if (state.value.coreClepsydra!!.id == null) {
                        clepsydraRepository.insertClepsydra(state.value.coreClepsydra!!)
                    } else {
                        clepsydraRepository.updateClepsydra(state.value.coreClepsydra!!)
                    }
                    loadClepsydraeForDate()
                }

                _state.update { it.copy(coreClepsydra = null) }
            }

            is ClepsydraScreenAction.ToggleDiatesi -> {
                _state.update {
                    it.copy(coreClepsydra = it.coreClepsydra?.invertiDiatesi())
                }
            }

            is ClepsydraScreenAction.ToggleHistory -> {
                _state.update { it.copy(showHistory = !it.showHistory) }
            }

            is ClepsydraScreenAction.OnSetName -> {
                _state.update { it.copy(coreClepsydra = it.coreClepsydra?.copy(name = action.newName)) }
            }

            is ClepsydraScreenAction.OnConfirmName -> {
                _state.update { it.copy(showNameDialog = false) }
            }

            is ClepsydraScreenAction.OnRestore -> {
                _state.update {
                    it.copy(
                        coreClepsydra = action.clepsydra.copy(lastStateChange = TimeSource.Monotonic.markNow()),
                        showHistory = false
                    )
                }
            }

            is ClepsydraScreenAction.OnDelete -> {
                viewModelScope.launch {
                    clepsydraRepository.deleteClepsydra(action.id)
                    loadClepsydraeForDate()
                }
            }

            is ClepsydraScreenAction.OnSetNote -> {
                _state.update { it.copy(coreClepsydra = it.coreClepsydra?.copy(note = action.newNote)) }
            }

            is ClepsydraScreenAction.OnPreviousDay -> {
                val currentDate = _state.value.currentLocalDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                _state.update { it.copy(currentLocalDate = currentDate.plus(DatePeriod(days = -1))) }
                loadClepsydraeForDate()
            }

            is ClepsydraScreenAction.OnNextDay -> {
                val currentDate = _state.value.currentLocalDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                _state.update { it.copy(currentLocalDate = currentDate.plus(DatePeriod(days = 1))) }
                loadClepsydraeForDate()
            }


            is ClepsydraScreenAction.OnPomodoroThresholdCrossed -> {
                _state.update { it.copy(pomodoroNotifying = true) }
                viewModelScope.launch {
                    notificationManager.sendPomodoroNotification(state.value.coreClepsydra!!)
                    delay(1.minutes)
                    _state.update { it.copy(pomodoroNotifying = false) }
                }
            }

            is ClepsydraScreenAction.OnCreateNoteAtTime -> {
                _state.update { it.copy(showTimedNoteDialog = true) }
            }

            ClepsydraScreenAction.OnCreateNoteAtTimeCancel -> TODO()
            is ClepsydraScreenAction.OnCreateNoteAtTimeConfirm -> TODO()
            is ClepsydraScreenAction.OnSetTags -> {}
            ClepsydraScreenAction.OnToggleShowJournal -> {
                _state.update { it.copy(showJournal = !it.showJournal) }
            }
        }
    }
}





