package oqk.ananke.clepsydrae.clepsydrae.presentation

import kotlinx.datetime.LocalDate
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.journal.domain.Journal
import kotlin.time.TimeMark

data class ClepsydraScreenState(
    val coreClepsydra: Clepsydra? = null,
    val secondaryClepsydrae: List<Clepsydra> = emptyList(),
    val journalOfDay: Journal? = null,
    val pastClepsydrae: List<Clepsydra> = emptyList(),
    val futureClepsydrae: List<Clepsydra> = emptyList(),
    val currentLocalDate: LocalDate? = null,
    val startOfDay: TimeMark? = null,
    val dateText: String = "",
    val showJournal: Boolean = false,
    val showHistory: Boolean = false,
    val showNameDialog: Boolean = false,
    val showTimedNoteDialog: Boolean = false,
    val pomodoroNotifying: Boolean = false,
    val showNotificationPermissionPopUp: Boolean = false,
)
