package oqk.ananke.clepsydrae.clepsydrae.presentation

data class Droplet(
    val progress: Float,
    val x: Float,
    val speed: Float,
    val size: Float,
    val drift: Float,
    val isStreak: Boolean
)