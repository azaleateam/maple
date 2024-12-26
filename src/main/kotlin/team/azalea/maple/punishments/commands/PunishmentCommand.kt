@file:Command(
    name = "punishment",
    description = "View an individual punishment",
    aliases = ["punishment"],
    usage = "<id>",
    permission = "maple.staff.punishments.view",
)

package team.azalea.maple.punishments.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import me.honkling.commando.common.annotations.Command
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import team.azalea.maple.maplePlugin
import team.azalea.maple.messageUtil
import team.azalea.maple.punishments.*
import team.azalea.maple.util.formatDate
import team.azalea.maple.util.mm
import team.azalea.maple.util.replaceTabs
import team.azalea.maple.util.sendKey

fun punishment(
    player: CommandSender, id: String
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val punishment = Punishments.get(id)

    if(punishment === null) {
        player.sendKey("punishments.invalidId", "id" to id)
        return@launch
    }

    val pages = mutableListOf<Component>()

    val moderator = Punishments.getPlayer(punishment.moderator)
    val target = Punishments.getPlayer(punishment.player)

    println("Target: $target | Moderator: $moderator")

    val punishmentType = PunishmentTypes.entries[punishment.type]
    val reasonText = punishment.getReasonString()

    val issuedAt = formatDate(punishment.getCreatedAt())
    val expiresAt = formatDate(punishment.getUpdatedAt())

    var status = "active"
    if(punishment.active == 0) status = "inactive"
    if(punishment.revertedAt != null) status ="reverted"

    val placeholders = mapOf(
        "id" to punishment.id,
        "moderator" to moderator.name,
        "player" to target.name,
        "action" to punishmentType.name,
        "type" to punishmentType.toString().lowercase(),
        "issued_at" to issuedAt,
        "expires_at" to expiresAt,
        "status" to status,
        "reason" to reasonText,
    )

    val overviewPage = messageUtil
        .translate("punishments.commands.view.overview", placeholders)
        .trimIndent()
        .replaceTabs()
        .mm()

    pages.add(overviewPage)

    val notes = punishment.notes ?: ""
    if(notes.isNotEmpty()) {
        maplePlugin.logger.info("Adding notes page")

        val notesPage = messageUtil
            .translate(
                "punishments.commands.view.notes",
                placeholders.plus("notes" to notes.replace("\n", "<newline>"))
            )
            .trimIndent()
            .replaceTabs()
            .mm()
        pages.add(notesPage)
    }

    val reverted = punishment.revertedAt != null
    if(reverted) {
        maplePlugin.logger.info("Adding reverted page")

        val revertedBy = Punishments.getPlayer(punishment.revertedBy!!)
        val reversionReason = punishment.revertedReason!!

        val newPlaceholders = placeholders
            .plus("reverted_by" to revertedBy.name)
            .plus("reverted_reason" to reversionReason)

        val revertedPage = messageUtil
            .translate(
                "punishments.commands.view.reverted",
                newPlaceholders
            )
            .trimIndent()
            .replaceTabs()
            .mm()
        pages.add(revertedPage)
    }

    val bookTitle = Component.empty()
    val book = Book.book(bookTitle, "Azalea Team".mm(), pages)
    player.openBook(book)
}