package oqk.ananke.clepsydrae.clepsydrae.presentation

import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra

data class ClepsydraState(
    val currentClepsydra: Clepsydra? = null,
    val name: String = "",
    val savedClepsydrae: List<Clepsydra> = emptyList(),
    val showHistory: Boolean = false,
    val showNameDialog: Boolean = false
)
