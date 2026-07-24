package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import oqk.ananke.clepsydrae.core.toTimeMark
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import oqk.ananke.clepsydrae.clepsydrae.domain.end
import oqk.ananke.clepsydrae.clepsydrae.domain.invertiDiatesi
import oqk.ananke.clepsydrae.core.NotificationManager
import oqk.ananke.clepsydrae.journal.domain.Journal
import oqk.ananke.clepsydrae.journal.domain.formatDate
import oqk.ananke.clepsydrae.journal.domain.JournalRepository
import oqk.ananke.clepsydrae.journal.domain.endOfDay
import oqk.ananke.clepsydrae.journal.domain.startOfDay
import oqk.ananke.clepsydrae.settings.domain.SettingsRepository
import kotlin.let
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface ClepsydraSideEffect {
    data class ShowPomodoroNotification(val clepsydra: Clepsydra) : ClepsydraSideEffect
}

@OptIn(ExperimentalTime::class)
class ClepsydraScreenViewModel(
    private val clepsydraRepository: ClepsydraRepository,
    private val settingsRepository: SettingsRepository,
    private val journalRepository: JournalRepository
    ) : ViewModel() {
    private val _state = MutableStateFlow(ClepsydraScreenState())
    val state = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<ClepsydraSideEffect>()
    val effect = _effect.asSharedFlow()
    
    init {
        loadDate(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    }

    @OptIn(ExperimentalTime::class)
    private fun loadDate(localDate: LocalDate) {
            _state.update { it.copy(
                currentLocalDate = localDate,
                dateText = formatDate(localDate),
                startOfDay = localDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds().toTimeMark()
            ) }

        loadClepsydraeForDate(localDate)
        loadJournalOfDay(localDate)
    }

    private var journalJob: Job? = null
    
    @OptIn(ExperimentalTime::class)
    private fun loadClepsydraeForDate(localDate: LocalDate? = _state.value.currentLocalDate) {
        viewModelScope.launch {
            val list = clepsydraRepository.getClepsydraeByDate(localDate!!)
            val past = list.filter { it.init.elapsedNow() >= Duration.ZERO }
            val future = list.filter { it.init.elapsedNow() < Duration.ZERO }
            _state.update { it.copy(
                pastClepsydrae = past, 
                futureClepsydrae = future
            ) }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun loadJournalOfDay(localDate: LocalDate? = _state.value.currentLocalDate) {
        // Cancel previous subscription if it exists
        journalJob?.cancel()

        // Start observing the new date
        journalJob = viewModelScope.launch {
            journalRepository.selectJournalOfDay(localDate!!).collect { updatedJournal ->

                _state.update { it.copy(journalOfDay = updatedJournal , journalReloadedTimes = state.value.journalReloadedTimes+1) }

                if(state.value.journalOfDay.entryAtInterval.size == 1 && state.value.journalOfDay.entryAtInterval[updatedJournal.entryAtInterval.keys.first()]?.second == null) {
                    journalRepository.insertEntry(localDate, startOfDay, entry = state.value.journalOfDay.entryAtInterval[startOfDay]?.first
                        ?: "", finTimeStamp = endOfDay)
                    loadJournalOfDay(localDate)
                }

            }
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
                    it.copy(
                        coreClepsydra = oqk.ananke.clepsydrae.clepsydrae.domain.CreateClepsydraUseCase()(
                            name = cc.name,
                            note = cc.note,
                            tags = cc.tags,
                            now = cc.now,
                            initHours = cc.initHours,
                            initMinutes = cc.initMinutes,
                            initSeconds = cc.initSeconds,
                            passiveGoal = cc.passiveGoal,
                            activeGoal = cc.activeGoal,
                            initOverride = cc.init,
                            finHours = cc.finHours,
                            finMinutes = cc.finMinutes,
                            finSeconds = cc.finSeconds,
                            finOverride = cc.fin
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
                loadDate(currentDate.plus(DatePeriod(days = -1)))
            }

            is ClepsydraScreenAction.OnNextDay -> {
                val currentDate = _state.value.currentLocalDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                loadDate(currentDate.plus(DatePeriod(days = 1)))
            }


            is ClepsydraScreenAction.OnPomodoroThresholdCrossed -> {
                _state.update { it.copy(pomodoroNotifying = true) }
                viewModelScope.launch {
                    _effect.emit(ClepsydraSideEffect.ShowPomodoroNotification(state.value.coreClepsydra!!))
                    delay(1.minutes)
                    _state.update { it.copy(pomodoroNotifying = false) }
                }
            }

            is ClepsydraScreenAction.OnCreateNoteAtTime -> {
                _state.update { it.copy(showJournal = true) }
                viewModelScope.launch {
                    val currentDate = _state.value.currentLocalDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                    journalRepository.insertEntry(
                        currentDate, action.time, entry = "",
                        finTimeStamp = null
                    )

                    loadJournalOfDay()
                }
            }

            is ClepsydraScreenAction.OnDeleteEntryAtTime -> {
                viewModelScope.launch {
                    val currentDate = _state.value.currentLocalDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

                    if(action.index != 0 && state.value.journalOfDay.entryAtInterval[action.prevItem!!.time]?.second != null)
                        journalRepository.updateEntry(
                            currentDate,
                            action.prevItem.time,
                            action.nextTime ?: endOfDay,
                            state.value.journalOfDay.entryAtInterval[action.prevItem.time]?.first ?: ""
                        )
                    journalRepository.deleteEntry(currentDate, action.selectedTime)
                    loadJournalOfDay()
                }
            }

            is ClepsydraScreenAction.OnSetEntryAtTime -> {
                viewModelScope.launch {
                    val currentDate = _state.value.currentLocalDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                    if(_state.value.journalOfDay.entryAtInterval.containsKey(action.time))
                        journalRepository.updateEntry(currentDate, action.time, action.entry.second, action.entry.first)
                    else journalRepository.insertEntry(currentDate, action.time,action.entry.second, action.entry.first)
                    loadJournalOfDay(currentDate)
                }
            }
            is ClepsydraScreenAction.OnSetTags -> {}
            is ClepsydraScreenAction.OnToggleShowJournal -> {
                _state.update { it.copy(showJournal = action.show) }
            }

            ClepsydraScreenAction.ReloadJournal -> {
                val currentDate = _state.value.currentLocalDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                _state.update { it.copy(journalOfDay = Journal(currentDate)) }
                loadJournalOfDay(currentDate)
            }
        }
    }
}





