package oqk.ananke.clepsydrae.clepsydrae.data

import oqk.ananke.clepsydrae.Database
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class ClepsydraRepositoryImpl(private val database: Database) : ClepsydraRepository {

    override suspend fun insertClepsydra(clepsydra: Clepsydra): Long {
        database.clessidraQueries.insertClepsydra(
            name = clepsydra.name,
            init_time = clepsydra.init.elapsedNow().inWholeMilliseconds,
            last_state_change = clepsydra.lastStateChange.elapsedNow().inWholeMilliseconds,
            total_active_time = clepsydra.totalActiveTime.inWholeMilliseconds,
            total_passive_time = clepsydra.totalPassiveTime.inWholeMilliseconds,
            is_active = if (clepsydra.isActive) 1L else 0L,
            session_id = clepsydra.sessionId
        )
        return database.clessidraQueries.selectAllClepsydrae().executeAsList().lastOrNull()?.id ?: 0L
    }

    override suspend fun getAllClepsydrae() : List<Clepsydra> {
        return database.clessidraQueries.selectAllClepsydrae().executeAsList().map { entity ->
            val now = TimeSource.Monotonic.markNow()
            Clepsydra(
                id = entity.id,
                name = entity.name,
                init = now,
                lastStateChange = now,
                totalActiveTime = entity.total_active_time.milliseconds,
                totalPassiveTime = entity.total_passive_time.milliseconds,
                isActive = entity.is_active == 1L,
                sessionId = entity.session_id
            )
        }
    }

    override suspend fun getClepsydraById(id: Long): Clepsydra? {
        val entity = database.clessidraQueries.selectClepsydraById(id).executeAsOneOrNull() ?: return null
        val now = TimeSource.Monotonic.markNow()
        return Clepsydra(
            id = entity.id,
            name = entity.name,
            init = now,
            lastStateChange = now,
            totalActiveTime = entity.total_active_time.milliseconds,
            totalPassiveTime = entity.total_passive_time.milliseconds,
            isActive = entity.is_active == 1L,
            sessionId = entity.session_id
        )
    }

    override suspend fun updateClepsydra(clepsydra: Clepsydra) {
        clepsydra.id?.let { id ->
            database.clessidraQueries.updateClepsydra(
                name = clepsydra.name,
                last_state_change = clepsydra.lastStateChange.elapsedNow().inWholeMilliseconds,
                total_active_time = clepsydra.totalActiveTime.inWholeMilliseconds,
                total_passive_time = clepsydra.totalPassiveTime.inWholeMilliseconds,
                is_active = if (clepsydra.isActive) 1L else 0L,
                id = id
            )
        }
    }

    override suspend fun deleteClepsydra(id: Long) {
        database.clessidraQueries.deleteClepsydra(id)
    }
}