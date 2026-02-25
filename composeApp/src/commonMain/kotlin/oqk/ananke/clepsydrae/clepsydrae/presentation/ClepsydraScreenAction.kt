package oqk.ananke.clepsydrae.clepsydrae.presentation

import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.journal.domain.TimeStamp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

sealed interface ClepsydraScreenAction {
    data class OnCreateClepsydra(
        val presetClepsydra: Clepsydra? = null,
        val name: String = presetClepsydra?.name ?: "",
        val note: String = presetClepsydra?.note ?: "",
        val tags: Set<String> = presetClepsydra?.tags ?: emptySet(),
        val now: TimeMark = TimeSource.Monotonic.markNow(),
        val initHours: Duration? = presetClepsydra?.init?.elapsedNow()?.inWholeHours?.hours,
        val initMinutes: Duration? = presetClepsydra?.init?.elapsedNow()?.inWholeMinutes?.minutes,
        val initSeconds: Duration? = presetClepsydra?.init?.elapsedNow()?.inWholeSeconds?.seconds,
        val init: TimeMark? = null,
        val finHours: Duration? = presetClepsydra?.fin?.elapsedNow()?.inWholeHours?.hours,
        val finMinutes: Duration? = presetClepsydra?.fin?.elapsedNow()?.inWholeMinutes?.minutes,
        val finSeconds: Duration? = presetClepsydra?.fin?.elapsedNow()?.inWholeSeconds?.seconds,
        val fin: TimeMark? = null,
        val activeGoal: Duration = presetClepsydra?.pomodoroActive ?: Duration.ZERO,
        val passiveGoal: Duration = presetClepsydra?.pomodoroPassive ?: Duration.ZERO,
        val startActive: Boolean = presetClepsydra?.isActive ?: false
    ) : ClepsydraScreenAction

    data class OnCreateNoteAtTime(val time: TimeStamp): ClepsydraScreenAction
    data class OnCreateNoteAtTimeConfirm(val entry: Pair<TimeStamp, String>): ClepsydraScreenAction
    data object OnCreateNoteAtTimeCancel: ClepsydraScreenAction
    data object OnToggleShowJournal: ClepsydraScreenAction
    data object OnFirstClepsydraCreation: ClepsydraScreenAction
    data object OnFirstClepsydraCreationOnResult: ClepsydraScreenAction
    data object OnCloseCoreClepsydra : ClepsydraScreenAction
    data object ToggleDiatesi : ClepsydraScreenAction
    data object ToggleHistory : ClepsydraScreenAction
    data class OnSetName(val newName: String) : ClepsydraScreenAction
    data class OnSetNote(val newNote: String) : ClepsydraScreenAction
    data class OnSetTags(val newTags: String) : ClepsydraScreenAction
    data object OnConfirmName : ClepsydraScreenAction
    data class OnRestore(val clepsydra: Clepsydra) : ClepsydraScreenAction
    data class OnDelete(val id: Long) : ClepsydraScreenAction
    data object OnPreviousDay : ClepsydraScreenAction
    data object OnNextDay : ClepsydraScreenAction
    data object OnPomodoroThresholdCrossed: ClepsydraScreenAction
}
