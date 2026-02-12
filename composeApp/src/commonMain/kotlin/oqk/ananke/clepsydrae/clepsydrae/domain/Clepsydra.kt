package oqk.ananke.clepsydrae.clepsydrae.domain

import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlin.time.TimeMark
data class Clepsydra(
    val id: Long? = null,
    val name: String? = "",
    val note: String? = null,
    val journal: String? = null,
    val init: TimeMark = TimeSource.Monotonic.markNow(),
    val lastStateChange: TimeMark = init,
    val activeTime: Duration = Duration.ZERO,
    val passiveTime: Duration = Duration.ZERO,
    val totalActiveTime: Duration = Duration.ZERO,
    val totalPassiveTime: Duration = Duration.ZERO,
    val isActive: Boolean = false,
    val ended: Boolean = false,
    val sessionId: Long? = null,
    val pomodoroActive: Duration = Duration.ZERO,
    val pomodoroPassive: Duration = Duration.ZERO,
    val fin: TimeMark? = null
)

fun Clepsydra.end(elapsed: Duration = lastStateChange.elapsedNow()): Clepsydra {
    return copy(
        isActive = false,
        ended = true,
        lastStateChange = lastStateChange + elapsed,
        passiveTime = Duration.ZERO,
        activeTime = Duration.ZERO,
        totalPassiveTime = if (isActive) totalPassiveTime else totalPassiveTime + elapsed,
        totalActiveTime = if (isActive) totalActiveTime + elapsed else totalActiveTime,
        fin = lastStateChange + elapsed
    )
}

fun Clepsydra.invertiDiatesi(elapsed: Duration = lastStateChange.elapsedNow()): Clepsydra {
    return if (isActive) {
        copy(
            passiveTime = Duration.ZERO,
            totalActiveTime = totalActiveTime + elapsed,
            isActive = false,
            lastStateChange = lastStateChange + elapsed
        )
    } else {
        copy(
            activeTime = Duration.ZERO,
            totalPassiveTime = totalPassiveTime + elapsed,
            isActive = true,
            lastStateChange = lastStateChange + elapsed
        )
    }
}

fun Clepsydra.strlapsed(elapsed: Duration = lastStateChange.elapsedNow()): String = dts(elapsed)

fun Clepsydra.shouldNotifyPomodoro(elapsed: Duration = lastStateChange.elapsedNow()): Boolean {
    val threshold = if (isActive) pomodoroActive else pomodoroPassive

    // 1. Safety check
    if (threshold <= Duration.ZERO || elapsed < threshold) return false

    // 2. Modulo in seconds to catch the repeat
    val elapsedSec = elapsed.inWholeSeconds
    val thresholdSec = threshold.inWholeSeconds
    val remainder = elapsedSec % thresholdSec

    // 3. The "Window" Logic:
    // Instead of checking for exactly 0, we check if we are in the first 2 seconds
    // of a new cycle. This ensures that even if the delay(1.s) is slightly late,
    // we still catch the trigger.
    return remainder in 0..5
}

fun dts(duration: Duration): String {
    return duration.toComponents { hours, minutes, seconds, _ ->
        val mm = minutes.toString().padStart(2, '0')
        val ss = seconds.toString().padStart(2, '0')
        if (hours > 0) "$hours:$mm:$ss" else "$mm:$ss"
    }
}

fun Duration.asText(): String = dts(this)