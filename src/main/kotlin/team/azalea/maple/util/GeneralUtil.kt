package team.azalea.maple.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatDate(instant: Instant): String {
    if(instant == Instant.MAX) return "Never"
    val formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}