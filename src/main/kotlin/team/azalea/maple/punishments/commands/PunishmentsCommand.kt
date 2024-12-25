@file:Command(
    name = "punishments",
    description = "View punishments of a player",
    aliases = ["puns"],
    usage = "punishments <player>",
    permission = "maple.staff.punishments.view",
)

package team.azalea.maple.punishments.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import me.honkling.commando.common.annotations.Command
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import team.azalea.maple.maplePlugin
import team.azalea.maple.messageUtil
import team.azalea.maple.punishments.*
import team.azalea.maple.util.mm
import team.azalea.maple.util.replaceTabs
import team.azalea.maple.util.sendKey

fun punishments(
    player: CommandSender, target: OfflinePlayer,
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val punishmentList = Punishments.list(target.uniqueId)

    val placeholders = mapOf(
        "player" to (target.name ?: "Unknown"),
    )

    if(punishmentList.isEmpty()) {
        player.sendKey("punishments.commands.view.all.empty", placeholders)
        return@launch
    }

    val sortedPunishments = punishmentList
        .sortedWith(compareByDescending<PunishmentData> { it.active }.thenBy { PunishmentTypes.isBan(it.type) })

    val punishments = sortedPunishments.joinToString("<newline>") {
        val type = PunishmentTypes.entries[it.type]
        val text = it.getReasonString()
        "<hover:show_text:'<p>View ${type.name.lowercase()} information</p>'><click:run_command:/punishment ${it.id}>$text</click></hover>"
    }.trimIndent().replaceTabs()

    val allPlaceholders = placeholders.plus("punishments" to punishments)
    val message = messageUtil
        .translate("punishments.commands.view.all", allPlaceholders)
        .trimIndent()
        .mm()

    val bookTitle = Component.empty()
    val book = Book.book(bookTitle, "Azalea Team".mm(), listOf(message))
    player.openBook(book)
}