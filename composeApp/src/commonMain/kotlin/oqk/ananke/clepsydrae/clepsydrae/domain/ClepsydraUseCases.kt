package oqk.ananke.clepsydrae.clepsydrae.domain

import kotlinx.datetime.LocalDate
import oqk.ananke.clepsydrae.journal.domain.JournalRepository
import oqk.ananke.clepsydrae.journal.domain.endOfDay
import oqk.ananke.clepsydrae.journal.domain.startOfDay
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class ClepsydraJournalUseCase(
    private val journalRepository: JournalRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend fun createInitialJournalEntryIfNeeded(localDate: LocalDate, currentEntriesSize: Int, firstEntryEnd: String?) {
        if (currentEntriesSize == 1 && firstEntryEnd == null) {
            journalRepository.insertEntry(localDate, startOfDay, entry = "", finTimeStamp = endOfDay)
        }
    }
}

class CreateClepsydraUseCase {
    @OptIn(ExperimentalTime::class)
    operator fun invoke(
        name: String?,
        note: String?,
        tags: Set<String>?,
        now: kotlin.time.TimeMark,
        initHours: kotlin.time.Duration?,
        initMinutes: kotlin.time.Duration?,
        initSeconds: kotlin.time.Duration?,
        passiveGoal: kotlin.time.Duration,
        activeGoal: kotlin.time.Duration,
        initOverride: kotlin.time.TimeMark?,
        finHours: kotlin.time.Duration?,
        finMinutes: kotlin.time.Duration?,
        finSeconds: kotlin.time.Duration?,
        finOverride: kotlin.time.TimeMark?
    ): Clepsydra {
        val fin = listOfNotNull(finHours, finMinutes, finSeconds)
            .reduceOrNull { acc, d -> acc + d }
            ?.takeIf { value -> value > Duration.ZERO }

        val init = listOfNotNull(initHours, initMinutes, initSeconds)
            .reduceOrNull { acc, d -> acc + d }?.takeIf { value -> value >= Duration.ZERO }
            
        val finalInit = initOverride ?: (now + (init ?: Duration.ZERO))

        return Clepsydra(
            name = name,
            note = note,
            tags = tags,
            init = finalInit,
            pomodoroPassive = passiveGoal,
            pomodoroActive = activeGoal,
            fin = finOverride ?: fin?.let { duration -> finalInit + duration }
        )
    }
}
