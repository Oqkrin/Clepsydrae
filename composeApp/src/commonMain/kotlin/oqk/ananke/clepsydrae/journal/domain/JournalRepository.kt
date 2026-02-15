package oqk.ananke.clepsydrae.journal.domain

interface JournalRepository {
    suspend fun insertEntry(day: String, init: TimeStamp, end: TimeStamp, entry: String)

    suspend fun selectJournalOfDay(day: String, startOfDay: TimeStamp, endOfDay: TimeStamp): Journal

    suspend fun updateEntry(day: String, init: TimeStamp, end: TimeStamp, entry: String)

    suspend fun deleteEntry(day: String, init: TimeStamp)
}