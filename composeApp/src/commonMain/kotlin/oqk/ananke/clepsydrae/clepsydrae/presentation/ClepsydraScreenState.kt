package oqk.ananke.clepsydrae.clepsydrae.presentation

import kotlinx.datetime.LocalDate
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra

data class ClepsydraScreenState(
    val currentClepsydra: Clepsydra? = null,
    val pastClepsydrae: List<Clepsydra> = emptyList(),
    val futureClepsydrae: List<Clepsydra> = emptyList(),
    val selectedDate: LocalDate? = null,
    val dateText: String = "",
    val showHistory: Boolean = false,
    val showNameDialog: Boolean = false,
    val pomodoroNotifying: Boolean = false,
    val shouldAskForNotificationPermission: Boolean = true
)
