package oqk.ananke.clepsydrae.clepsydrae.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import oqk.ananke.clepsydrae.Database
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import oqk.ananke.clepsydrae.core.TimeScope
import oqk.ananke.clepsydrae.core.toTimeMark

class ClepsydraRepositoryImpl(private val database: Database) : ClepsydraRepository {

    override suspend fun insertClepsydra(clepsydra: Clepsydra): Long {
        with(TimeScope()) {
            database.clessidraQueries.insertClepsydra(
                name = clepsydra.name,
                init_time = clepsydra.init.toEpochMillis(),
                last_state_change = clepsydra.lastStateChange.toEpochMillis(),
                total_active_time = clepsydra.totalActiveTime.inWholeMilliseconds,
                total_passive_time = clepsydra.totalPassiveTime.inWholeMilliseconds,
                is_active = clepsydra.isActive.toBooleanLong(),
                session_id = clepsydra.sessionId,
                note = clepsydra.note,
                tags = clepsydra.tags?.joinToString(prefix = "#", separator = " #"),
                ended = clepsydra.ended.toBooleanLong(),
                pomodoro_active = clepsydra.pomodoroActive.inWholeMilliseconds,
                pomodoro_passive = clepsydra.pomodoroPassive.inWholeMilliseconds,
                fin = clepsydra.fin?.toEpochMillis()
            )
        }
        return database.clessidraQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun getAllClepsydrae() : List<Clepsydra> {
        return with(TimeScope()) {
            database.clessidraQueries.selectAllClepsydrae().executeAsList().map { sqliteToKotlin(it) }
        }
    }

    override suspend fun getClepsydraById(id: Long): Clepsydra {
        val entity = database.clessidraQueries.selectClepsydraById(id).executeAsOneOrNull() ?: return Clepsydra()
        return with(TimeScope()) { sqliteToKotlin(entity) }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getClepsydraeByDate(date: LocalDate): List<Clepsydra> {
        val startOfDay = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endOfDay = startOfDay + 1.days.inWholeMilliseconds
        return with(TimeScope()) {
            database.clessidraQueries.selectClepsydraeByDate(startOfDay, endOfDay).executeAsList().map { sqliteToKotlin(it) }
        }
    }

    override suspend fun updateClepsydra(clepsydra: Clepsydra) {
        clepsydra.id?.let { id ->
            with(TimeScope()) {
                database.clessidraQueries.updateClepsydra(
                    name = clepsydra.name,
                    last_state_change = clepsydra.lastStateChange.toEpochMillis(),
                    total_active_time = clepsydra.totalActiveTime.inWholeMilliseconds,
                    total_passive_time = clepsydra.totalPassiveTime.inWholeMilliseconds,
                    is_active = clepsydra.isActive.toBooleanLong(),
                    id = id,
                    note = clepsydra.note,
                    tags = clepsydra.tags?.joinToString(prefix = "#", separator = " #"),
                    ended = clepsydra.ended.toBooleanLong(),
                    pomodoro_active = clepsydra.pomodoroActive.inWholeMilliseconds,
                    pomodoro_passive = clepsydra.pomodoroPassive.inWholeMilliseconds,
                    fin = clepsydra.fin?.toEpochMillis()
                )
            }
        }
    }

    override suspend fun deleteClepsydra(id: Long) {
        database.clessidraQueries.deleteClepsydra(id)
    }

    private fun TimeScope.sqliteToKotlin(entity: oqk.ananke.clepsydrae.Clepsydra): Clepsydra {
        return Clepsydra(
            id = entity.id,
            name = entity.name,
            init = entity.init_time.toTimeMark(),
            lastStateChange = entity.last_state_change.toTimeMark(),
            totalActiveTime = entity.total_active_time.milliseconds,
            totalPassiveTime = entity.total_passive_time.milliseconds,
            isActive = entity.is_active.toBool(),
            sessionId = entity.session_id,
            note = entity.note,
            tags = entity.tags?.split("#")?.filter { it.isNotEmpty() }
                ?.map { it.trim().replace(' ', '_') + " " }?.toSet(),
            ended = entity.ended.toBool(),
            pomodoroActive = entity.pomodoro_active.milliseconds,
            pomodoroPassive = entity.pomodoro_passive.milliseconds,
            fin = entity.fin?.toTimeMark()
        )
    }

}



fun Long.toBool(): Boolean = this != 0L
fun Boolean.toBooleanLong(): Long = if (this) 1L else 0L