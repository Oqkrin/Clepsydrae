package oqk.ananke.clepsydrae.clepsydrae.presentation

import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra

sealed interface ClepsydraAction {
    data object OnSimpleCreate : ClepsydraAction
    data object OnCreateWithName : ClepsydraAction
    data object OnClose : ClepsydraAction
    data object ToggleDiatesi : ClepsydraAction
    data object ToggleHistory : ClepsydraAction
    data class OnSetName(val newName: String) : ClepsydraAction
    data class OnSetNote(val newNote: String) : ClepsydraAction
    data object OnConfirmName : ClepsydraAction
    data class OnRestore(val clepsydra: Clepsydra) : ClepsydraAction
    data class OnDelete(val id: Long) : ClepsydraAction
    data object OnPreviousDay : ClepsydraAction
    data object OnNextDay : ClepsydraAction
}
