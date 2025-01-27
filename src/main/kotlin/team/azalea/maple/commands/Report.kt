@file:Command("report")

package team.azalea.maple.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Ignore
import me.honkling.commando.common.annotations.Optional
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import team.azalea.maple.maplePlugin
import team.azalea.maple.messageUtil
import team.azalea.maple.reports.Reports
import team.azalea.maple.reports.reportConfig
import team.azalea.maple.util.handleCooldown
import team.azalea.maple.util.isConfirmed
import team.azalea.maple.util.mm
import team.azalea.maple.util.sendKey

private object ReportCooldowns {
    /**
     * Prevents the player from creating more than one report in 15 seconds.
     */
    @Ignore
    fun make(player: Player) = handleCooldown {
        key = "report-${player.uniqueId}"
        duration = 1000L * 15
    }

    /**
     * Prevents the player from reporting someone twice in a row.
     */
    @Ignore
    fun makePlayer(player: Player, target: Player) = handleCooldown {
        key = "reporting-${player.uniqueId}-${target.uniqueId}"
        // 1 hour
        duration = (1000L * 60) * 60
    }
}

@Ignore
private fun initialCommand(player: Player, target: Player) {
    val reportList = reportConfig.reports.map { (key, value) ->
        "- <click:run_command:/report ${target.name} $key>${value.reason}</click>"
    }

    val pages = mutableListOf<Component>()

    val placeholders = mapOf(
        "player" to target.name,
    )

    val reportChunks = reportList.chunked(8)
    reportChunks.forEachIndexed { index, chunk ->
        val reports = chunk.joinToString("<newline>")
        val newPlaceholders = placeholders + mapOf(
            "reports" to reports,
            "current_page" to index + 1,
            "pages" to reportChunks.size,
        )

        val pageText = messageUtil
            .translate("commands.report.initial", newPlaceholders)
            .trimIndent()
        pages.add(pageText.mm())
    }

    val book = Book.book(Component.empty(), "Azalea Team".mm(), pages)
    player.openBook(book)
}

@Ignore
private fun confirmReport(player:  Player, target: Player, short: String) {
    val info = reportConfig.reports[short] ?: throw IllegalArgumentException("Invalid report type: $short")

    val pages = mutableListOf<Component>()

    val placeholders = mapOf(
        "player" to target.name,
        "reason" to info.reason,
        "short" to short
    )

    val pageText = messageUtil
        .translate("commands.report.confirm", placeholders)
        .trimIndent()
    pages.add(pageText.mm())

    val book = Book.book(Component.empty(), "Azalea Team".mm(), pages)
    player.openBook(book)
}

@Ignore
private fun finishReport(player: Player, target: Player, short: String) = maplePlugin.launch(Dispatchers.IO) {
    if(ReportCooldowns.make(player).isActive()) {
        player.sendKey("commands.report.error", "reason" to "You've recently reported someone!")
        return@launch
    }

    if(ReportCooldowns.makePlayer(player, target).isActive()) {
        player.sendKey("commands.report.error", "reason" to "You've recently reported this player!")
        return@launch
    }

    val info = reportConfig.reports[short] ?: throw IllegalArgumentException("Invalid report type: $short")

    ReportCooldowns.make(player).startCooldown()
    ReportCooldowns.makePlayer(player, target).startCooldown()

    player.sendKey("commands.report.success")

    Reports.sendDiscordLog(player, target, info.reason)

    val placeholders = mapOf(
        "player" to player.name,
        "target" to target.name,
        "reason" to info.reason,
    )
    Bukkit.getOnlinePlayers().filter { it.hasPermission("maple.staff") }
        .forEach { it.sendKey("commands.report.log", placeholders) }
}

fun report(
    player: Player, target: Player,
    @Optional short: String?, @Optional confirm: String?,
) {
    if(player.uniqueId == target.uniqueId) {
        player.sendKey("commands.report.error", "reason" to "You can't report yourself!")
        return
    }

   if(short === null) {
       initialCommand(player, target)
       return
   }

    if(isConfirmed(confirm)) {
        try {
            finishReport(player, target, short.toString())
        } catch (e: Exception) {
            player.sendKey("commands.report.error", "reason" to e.message!!)
        }
        return
    }

    try {
        confirmReport(player, target, short.toString())
    } catch (e: Exception) {
        player.sendKey("commands.report.error", "reason" to e.message!!)
    }
}