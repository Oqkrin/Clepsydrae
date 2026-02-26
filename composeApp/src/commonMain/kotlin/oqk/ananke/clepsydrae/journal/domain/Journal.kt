package oqk.ananke.clepsydrae.journal.domain

import kotlinx.datetime.LocalDate
import java.util.TreeMap
import kotlin.time.ExperimentalTime

typealias TimeStamp = String
const val startOfDay: String = "00:00:00"
const val endOfDay: TimeStamp = "23:59:59"

sealed interface TimelineItem {
    val time: String

    data class ExistingEntry(
        override val time: TimeStamp,
        val content: String,
        val endTime: TimeStamp?
    ) : TimelineItem

    data class Gap(
        override val time: TimeStamp
    ) : TimelineItem
}

data class Journal @OptIn(ExperimentalTime::class) constructor(
    val day: LocalDate?,
    val entryAtInterval: MutableMap<TimeStamp, Pair<String, TimeStamp?>> = TreeMap()
) {
    constructor(dayNum: Int, monthNum: Int, year: Int) : this(LocalDate(year, monthNum, dayNum))

    init {
        entryAtInterval[startOfDay] = "" to endOfDay
    }

    override fun toString(): String {
        var journalText = day?.asText() ?: ""

        var lastEnd: TimeStamp? = null

        entryAtInterval.forEach { (init, pair) ->

            journalText += (if(lastEnd != init) "\n$init" else "") + "\n${pair.first} \n${if(pair.second != null) pair.second else ""}"

            lastEnd = pair.second

        }

        return journalText
    }

    fun addEntry(initTimeStamp: TimeStamp, entry: String = "", finTimeStamp: TimeStamp? = null) {
        entryAtInterval[initTimeStamp] = entry to finTimeStamp
    }

    // 1. Define specific types for the UI to render easily


    // 2. The Logic to "Zip" entries and gaps together chronologically
    fun buildTimeline(): List<TimelineItem> {
        if (this.entryAtInterval.isEmpty()) {
            // Default start if empty
            return listOf(TimelineItem.Gap(startOfDay))
        }

        val items = mutableListOf<TimelineItem>()
        val sortedEntries = this.entryAtInterval.toSortedMap()
        val entryIterator = sortedEntries.iterator()

        // Track the last processed time to prevent out-of-order items
        var lastTimeProcessed = startOfDay

        while (entryIterator.hasNext()) {
            val (startTime, data) = entryIterator.next()
            val (content, endTime) = data

            // OPTIONAL: If there's a huge unaccounted gap before this entry,
            // you could insert a Gap item here too.
            // For now, we assume the user fills gaps sequentially.

            items.add(TimelineItem.ExistingEntry(startTime, content, endTime))
            lastTimeProcessed = startTime

            // LOGIC: If this entry has an end time, does it create a gap?
            if (endTime != null) {
                // Peek at the next entry's start time (if exists)
                // We can't easily peek an iterator, so we rely on the loop order.
                // A simpler way is to compare with the "next key" if we had a list.

                // Simplified approach: Just add the Gap.
                // The "Sort" step later will handle order,
                // but we must check if this gap overlaps with the next entry.
                items.add(TimelineItem.Gap(endTime))
            }
        }

        // 3. Clean up: Remove Gaps that overlap with existing entries
        // and sort everything by time string.
        val distinctItems = items
            .sortedBy { it.time } // "Pushed to later" logic handled by sort
            .distinctBy { it.time } // distinct removes duplicate times
            .filter { item ->
                // If a Gap exists at the exact same time as an Entry,
                // the Entry wins (remove the Gap).
                if (item is TimelineItem.Gap) {
                    !sortedEntries.containsKey(item.time)
                } else {
                    true
                }
            }

        return distinctItems
    }

}

fun LocalDate.asText() = formatDate(this)

fun formatDate(date: LocalDate): String {
    val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$dayName ${date.day} $monthName ${date.year}"
}
