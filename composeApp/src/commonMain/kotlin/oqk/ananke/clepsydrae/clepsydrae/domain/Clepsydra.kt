package oqk.ananke.clepsydrae.clepsydrae.domain

import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlin.time.TimeMark
data class Clepsydra(
    val id: Long? = null,
    val name: String? = null,
    val init: TimeMark = TimeSource.Monotonic.markNow(),
    val lastStateChange: TimeMark = init,
    val activeTime: Duration = Duration.ZERO,
    val passiveTime: Duration = Duration.ZERO,
    val totalActiveTime: Duration = Duration.ZERO,
    val totalPassiveTime: Duration = Duration.ZERO,
    val isActive: Boolean = false,
    val sessionId: Long? = null
)

fun Clepsydra.invertiDiatesi(): Clepsydra {
    val elapsed = lastStateChange.elapsedNow()
    return if (isActive) {
        copy(
            passiveTime = Duration.ZERO,
            totalActiveTime = totalActiveTime + elapsed,
            isActive = false,
            lastStateChange = TimeSource.Monotonic.markNow()
        )
    } else {
        copy(
            activeTime = Duration.ZERO,
            totalPassiveTime = totalPassiveTime + elapsed,
            isActive = true,
            lastStateChange = TimeSource.Monotonic.markNow()
        )
    }
}

fun Clepsydra.strlapsed(): String = dts(lastStateChange.elapsedNow())

fun dts(duration: Duration): String {
    return duration.toComponents { hours, minutes, seconds, _ ->
        val mm = minutes.toString().padStart(2, '0')
        val ss = seconds.toString().padStart(2, '0')
        if (hours > 0) "$hours:$mm:$ss" else "$mm:$ss"
    }
}