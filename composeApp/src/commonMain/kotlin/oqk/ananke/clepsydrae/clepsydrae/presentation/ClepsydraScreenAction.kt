package oqk.ananke.clepsydrae.clepsydrae.presentation

import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra

sealed interface ClepsydraScreenAction {
    data object OnSimpleCreate : ClepsydraScreenAction
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
}
