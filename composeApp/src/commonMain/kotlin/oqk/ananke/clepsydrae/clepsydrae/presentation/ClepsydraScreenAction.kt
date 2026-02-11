package oqk.ananke.clepsydrae.clepsydrae.presentation

import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.core.NotificationManager
import oqk.ananke.clepsydrae.settings.presentation.SettingsScreenViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

sealed interface ClepsydraScreenAction {
    data class OnCreateClepsydra(
        val presetClepsydra: Clepsydra? = null,
        val name: String = presetClepsydra?.name ?: "",
        val note: String = presetClepsydra?.note ?: "",
        val hours: Duration? = presetClepsydra?.fin?.elapsedNow()?.inWholeHours?.hours,
        val minutes: Duration? = presetClepsydra?.fin?.elapsedNow()?.inWholeMinutes?.minutes,
        val seconds: Duration? = presetClepsydra?.fin?.elapsedNow()?.inWholeSeconds?.seconds,
        val activeGoal: Duration = presetClepsydra?.pomodoroActive ?: Duration.ZERO,
        val passiveGoal: Duration = presetClepsydra?.pomodoroPassive ?: Duration.ZERO,
        val isActive: Boolean = presetClepsydra?.isActive ?: false
    ) : ClepsydraScreenAction

    data class NotificationsPermissioner(
    val notificationManager: NotificationManager,
    val isFirstClepsydra: Boolean,
    val ssvm: SettingsScreenViewModel
    ) : ClepsydraScreenAction
    data object OnCreateWithName : ClepsydraScreenAction
    data object OnClose : ClepsydraScreenAction
    data object ToggleDiatesi : ClepsydraScreenAction
    data object ToggleHistory : ClepsydraScreenAction
    data class OnSetName(val newName: String) : ClepsydraScreenAction
    data class OnSetNote(val newNote: String) : ClepsydraScreenAction
    data object OnConfirmName : ClepsydraScreenAction
    data class OnRestore(val clepsydra: Clepsydra) : ClepsydraScreenAction
    data class OnDelete(val id: Long) : ClepsydraScreenAction
    data object OnPreviousDay : ClepsydraScreenAction
    data object OnNextDay : ClepsydraScreenAction
    data class OnPomodoroThresholdCrossed(val notificationManager: NotificationManager): ClepsydraScreenAction
}
