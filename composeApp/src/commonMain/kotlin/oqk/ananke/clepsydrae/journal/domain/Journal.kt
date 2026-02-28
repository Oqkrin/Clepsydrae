package oqk.ananke.clepsydrae.journal.domain

import kotlinx.datetime.LocalDate
import java.util.TreeMap
import kotlin.time.ExperimentalTime

typealias TimeStamp = String
const val startOfDay: String = "00:00:00"
const val endOfDay: TimeStamp = "23:59:59"

sealed interface TimelineItem {
    val time: String
    val depth: Int // Add Depth for indentation

    data class ExistingEntry(
        override val time: TimeStamp,
        val content: String,
        val endTime: TimeStamp?,
        override val depth: Int
    ) : TimelineItem

    data class Gap(
        override val time: TimeStamp,
        override val depth: Int
    ) : TimelineItem
}

data class Journal @OptIn(ExperimentalTime::class) constructor(
    val day: LocalDate?,
    val entryAtInterval: MutableMap<TimeStamp, Pair<String, TimeStamp?>> = TreeMap()
) {
    // ... constructors and toString

    fun addEntry(initTimeStamp: TimeStamp, entry: String = "", finTimeStamp: TimeStamp? = null) {
        entryAtInterval[initTimeStamp] = entry to finTimeStamp
    }

    fun buildTimeline(): List<TimelineItem> {
        val rootItems = mutableListOf<TimelineItem>()

        // 1. Sort all entries by Start Time
        val sortedEntries = entryAtInterval.entries
            .sortedBy { it.key }
            .map { it.key to it.value } // List of (Start, (Content, End))

        // 2. Recursive function to process entries and their "children"
        fun processRecursive(
            entries: List<Pair<String, Pair<String, String?>>>,
            currentDepth: Int,
            parentEnd: String?
        ): List<Pair<String, Pair<String, String?>>> {

            var remainingEntries = entries

            while (remainingEntries.isNotEmpty()) {
                val current = remainingEntries.first()
                val (startTime, data) = current
                val (content, endTime) = data

                // STOP CONDITION:
                // If this entry starts AFTER the parent ends, it's not a child. Return it to the caller.
                if (parentEnd != null && startTime >= parentEnd) {
                    return remainingEntries
                }

                // PROCESS CURRENT:
                rootItems.add(TimelineItem.ExistingEntry(startTime, content, endTime, currentDepth))

                // CONSUME: Remove current from list
                remainingEntries = remainingEntries.drop(1)

                // RECURSE:
                // Process subsequent entries. If they start before 'endTime', they are children (depth + 1).
                // They will be consumed inside this call.
                remainingEntries = processRecursive(remainingEntries, currentDepth + 1, endTime)

                // CLOSING GAP:
                // If this entry has a specific end time, add a Gap marker there
                // BUT only if it ends before the parent ends (or if no parent)
                if (endTime != null) {
                    // Only add a gap if the parent doesn't close exactly here (deduplication)
                    if (parentEnd == null || endTime < parentEnd) {
                        rootItems.add(TimelineItem.Gap(endTime, currentDepth))
                    }
                }
            }
            return emptyList()
        }

        processRecursive(sortedEntries, 0, null)

        // Final cleanup: distinct by time to prevent double-rendering if an entry ends exactly when another starts
        return rootItems.distinctBy { it.time }
    }
}

fun LocalDate.asText() = formatDate(this)

fun formatDate(date: LocalDate): String {
    val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$dayName ${date.day} $monthName ${date.year}"
}
