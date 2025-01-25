package team.azalea.maple.util

import org.bukkit.scheduler.BukkitTask
import team.azalea.maple.maplePlugin
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

fun formatDate(instant: Instant): String {
    if(instant == Instant.MAX) return "Never"
    val formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

fun formatDateWithSeconds(instant: Instant): String {
    if(instant == Instant.MAX) return "Never"
    val formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)

fun infinity(code: Consumer<BukkitTask>, delay: Int) {
    maplePlugin.server.scheduler.runTaskTimer(maplePlugin, { it: BukkitTask ->
        code.accept(it)
}, 0, delay.toLong())