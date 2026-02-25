package oqk.ananke.clepsydrae.journal.domain

import kotlinx.datetime.LocalDate

interface JournalRepository {
    suspend fun insertEntry(day: LocalDate, initTimeStamp: TimeStamp, finTimeStamp: TimeStamp?, entry: String)
    suspend fun selectJournalOfDay(day: LocalDate): Journal
    suspend fun updateEntry(day: LocalDate, initTimeStamp: TimeStamp, finTimeStamp: TimeStamp?, entry: String)
    suspend fun deleteEntry(day: LocalDate, initTimeStamp: TimeStamp)
}