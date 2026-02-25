package oqk.ananke.clepsydrae.journal.data

import androidx.compose.ui.util.normalizedAngleSin
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number
import oqk.ananke.clepsydrae.Database
import oqk.ananke.clepsydrae.journal.domain.Journal
import oqk.ananke.clepsydrae.journal.domain.JournalRepository
import oqk.ananke.clepsydrae.journal.domain.TimeStamp
import kotlin.time.Duration.Companion.days

class JournalRepositoryImpl(private val database: Database): JournalRepository {
    override suspend fun insertEntry(
        day: LocalDate,
        initTimeStamp: TimeStamp,
        finTimeStamp: TimeStamp?,
        entry: String
    ) {
        database.journalQueries.insertEntry(day.day.toLong(),day.dayOfWeek.name, day.month.number.toLong(), day.month.name, day.year.toLong(), initTimeStamp, finTimeStamp, entry)
    }

    override suspend fun selectJournalOfDay(day: LocalDate): Journal {
        val prevDay = day - DatePeriod(days = 1)

        val journalOfDay = Journal(day)

        val entriesOfDay = database.journalQueries.selectJournalOfDay(day.day.toLong(), day.month.number.toLong(), day.year.toLong(), prevDay.day.toLong(), prevDay.month.number.toLong(), prevDay.year.toLong())

        entriesOfDay.executeAsList().forEach {
            journalOfDay.addEntry(it.initTimeStamp, it.entry ?: "", it.finTimeStamp)
        }

        return journalOfDay

    }

    override suspend fun updateEntry(day: LocalDate, initTimeStamp: TimeStamp, finTimeStamp: TimeStamp?, entry: String) {
    }

    override suspend fun deleteEntry(day: LocalDate, initTimeStamp: TimeStamp) {
    }

}