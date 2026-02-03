package oqk.ananke.clepsydrae.clepsydrae.presentation

import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra

sealed interface ClepsydraAction {
    data object Create : ClepsydraAction
    data object CreateWithName : ClepsydraAction
    data object Close : ClepsydraAction
    data object Toggle : ClepsydraAction
    data object ToggleHistory : ClepsydraAction
    data class SetName(val newName: String) : ClepsydraAction
    data object ConfirmName : ClepsydraAction
    data class Restore(val clepsydra: Clepsydra) : ClepsydraAction
    data class Delete(val id: Long) : ClepsydraAction
}
