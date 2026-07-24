package oqk.ananke.clepsydrae.core

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class TimeScope {
    val now: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val mark: TimeMark = TimeSource.Monotonic.markNow()
    fun Long.toTimeMark(): TimeMark = mark - (now - this).milliseconds
    fun TimeMark.toEpochMillis(): Long = now - elapsedNow().inWholeMilliseconds
}

@OptIn(ExperimentalTime::class)
fun Long.toTimeMark(): TimeMark {
    val offset = (kotlin.time.Clock.System.now().toEpochMilliseconds() - this).milliseconds
    return TimeSource.Monotonic.markNow() - offset
}

@OptIn(ExperimentalTime::class)
fun TimeMark.toEpochMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds() - elapsedNow().inWholeMilliseconds
