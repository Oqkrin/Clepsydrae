package oqk.ananke.clepsydrae.journal.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number
import oqk.ananke.clepsydrae.Database
import oqk.ananke.clepsydrae.journal.domain.Journal
import oqk.ananke.clepsydrae.journal.domain.JournalRepository
import oqk.ananke.clepsydrae.journal.domain.TimeStamp

class JournalRepositoryImpl(private val database: Database): JournalRepository {
    override suspend fun insertEntry(
        day: LocalDate,
        initTimeStamp: TimeStamp,
        finTimeStamp: TimeStamp?,
        entry: String
    ) {
        database.journalQueries.insertEntry(day.day.toLong(),day.dayOfWeek.name, day.month.number.toLong(), day.month.name, day.year.toLong(), initTimeStamp, finTimeStamp, entry)
    }

    override fun selectJournalOfDay(day: LocalDate): Flow<Journal> {
        val prevDay = day - DatePeriod(days = 1)

        return database.journalQueries.selectJournalOfDay(
            day.day.toLong(), day.month.number.toLong(), day.year.toLong(),
            prevDay.day.toLong(), prevDay.month.number.toLong(), prevDay.year.toLong()
        )
            .asFlow()
            .mapToList(Dispatchers.IO) // Automatically re-queries on DB changes
            .map { entries ->
                val journal = Journal(day)
                entries.forEach {
                    journal.addEntry(it.initTimeStamp, it.entry ?: "", it.finTimeStamp)
                }
                journal
            }
    }

    override suspend fun updateEntry(day: LocalDate, initTimeStamp: TimeStamp, finTimeStamp: TimeStamp?, entry: String) {
        database.journalQueries.updateEntry(
            finTimeStamp, entry, day.day.toLong(),
            monthNum = day.month.number.toLong(),
            year = day.year.toLong(),
            init = initTimeStamp,
        )
    }

    override suspend fun deleteEntry(day: LocalDate, initTimeStamp: TimeStamp) {
        database.journalQueries.deleteEntry(day.day.toLong(), day.month.number.toLong(), day.year.toLong(), initTimeStamp)
    }

}