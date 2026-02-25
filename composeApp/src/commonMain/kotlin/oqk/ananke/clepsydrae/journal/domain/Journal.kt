package oqk.ananke.clepsydrae.journal.domain

import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

typealias TimeStamp = String

data class Journal @OptIn(ExperimentalTime::class) constructor(
    val day: LocalDate,
    val entryAtInterval: Map<TimeStamp, Pair<String, TimeStamp?>> = sortedMapOf(),
) {
    constructor(dayNum: Int, monthNum: Int, year: Int) : this(LocalDate(year, monthNum, dayNum))

    override fun toString(): String {
        var journalText = day.asText()

        var lastEnd: TimeStamp? = null

        entryAtInterval.forEach { (init, pair) ->

            journalText += (if(lastEnd != init) "\n$init" else "") + "\n${pair.first} + \n${pair.second}"

            lastEnd = pair.second

        }

        return journalText
    }

    fun addEntry(initTimeStamp: TimeStamp, entry: String = "", finTimeStamp: TimeStamp? = null) {
        entryAtInterval + (initTimeStamp to (entry to finTimeStamp))
    }
}

fun LocalDate.asText() = formatDate(this)

fun formatDate(date: LocalDate): String {
    val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$dayName ${date.day} $monthName ${date.year}"
}
