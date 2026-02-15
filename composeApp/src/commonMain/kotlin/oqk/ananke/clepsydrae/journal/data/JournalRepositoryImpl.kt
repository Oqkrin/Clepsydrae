package oqk.ananke.clepsydrae.journal.data

import oqk.ananke.clepsydrae.Database
import oqk.ananke.clepsydrae.journal.domain.Journal
import oqk.ananke.clepsydrae.journal.domain.JournalRepository
import oqk.ananke.clepsydrae.journal.domain.TimeStamp

class JournalRepositoryImpl(private val database: Database): JournalRepository {
    override suspend fun insertEntry(
        day: String,
        init: TimeStamp,
        end: TimeStamp,
        entry: String
    ) {
        database.journalQueries.insertEntry(day, init, end, entry)
    }

    override suspend fun selectJournalOfDay(
        day: String,
        startOfDay: TimeStamp,
        endOfDay: TimeStamp
    ): Journal {

        val journals = database.journalQueries.selectJournalOfDay(day, startOfDay, endOfDay).executeAsList()

        val entryMap: Map<TimeStamp, String> = journals.associate { it.init_ to it.entry!! }

        return Journal(day, entryMap)
    }

    override suspend fun updateEntry(
        day: String,
        init: TimeStamp,
        end: TimeStamp,
        entry: String
    ) {
        database.journalQueries.updateEntry(day, init, end, entry)
    }

    override suspend fun deleteEntry(day: String, init: TimeStamp) {
        database.journalQueries.deleteEntry(day, init)
    }

}