package oqk.ananke.clepsydrae.journal.domain

typealias TimeStamp = String

data class Journal(
    val day: String? = null,
    val timedEntry: Map<TimeStamp, String> = mapOf(),
    val intervalledEntry: Map<TimeStamp, Pair<String, TimeStamp?>> = mapOf(),
    val asyncEntry: List<Pair<String, Pair<TimeStamp?, TimeStamp?>>> = listOf()
)