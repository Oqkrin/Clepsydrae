package oqk.ananke.clepsydrae.journal.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface JournalRepository {
    suspend fun insertEntry(day: LocalDate, initTimeStamp: TimeStamp, finTimeStamp: TimeStamp?, entry: String)
    fun selectJournalOfDay(day: LocalDate): Flow<Journal>
    suspend fun updateEntry(day: LocalDate, initTimeStamp: TimeStamp, finTimeStamp: TimeStamp?, entry: String)
    suspend fun deleteEntry(day: LocalDate, initTimeStamp: TimeStamp)
}