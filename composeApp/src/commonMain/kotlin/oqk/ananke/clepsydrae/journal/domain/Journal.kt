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
        val items = mutableListOf<TimelineItem>()
        val sorted = entryAtInterval.entries.sortedBy { it.key }
        
        fun process(entries: List<Map.Entry<String, Pair<String, String?>>>, depth: Int, parentEnd: String?): Int {
            var i = 0
            while (i < entries.size) {
                val (start, data) = entries[i]
                val (content, end) = data
                
                if (parentEnd != null && start >= parentEnd) return i
                
                items.add(TimelineItem.ExistingEntry(start, content, end, depth))
                i++
                
                if (end != null) {
                    i += process(entries.subList(i, entries.size), depth + 1, end)
                    if ( !entryAtInterval.containsKey(end) ) {
                        items.add(TimelineItem.Gap(end, depth))
                    }
                }
            }
            return i
        }
        
        process(sorted, 0, null)
        return items.sortedBy { it.time }
    }
}

fun LocalDate.asText() = formatDate(this)

fun formatDate(date: LocalDate): String {
    val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$dayName ${date.day} $monthName ${date.year}"
}
