@file:Command(
    name = "punish",
    description = "Punishes a player",
    usage = "punish <player> [reason] [confirm]",
    permission = "maple.staff.punishments.punish",
)

package team.azalea.maple.punishments.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Ignore
import me.honkling.commando.common.annotations.Optional
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import team.azalea.maple.maplePlugin
import team.azalea.maple.messageUtil
import team.azalea.maple.punishments.*
import team.azalea.maple.types.PunishmentShort
import team.azalea.maple.util.isConfirmed
import team.azalea.maple.util.mm
import team.azalea.maple.util.sendKey


@Ignore
private fun initialCommand(player: User, target: User) {
    maplePlugin.logger.info("Initializing punishment for $target")

    val punishmentsList = punishmentConfig.punishments.map { (key, value) ->
        "- <click:run_command:/punish ${target.name} $key>${value.shortReason}</click>"
    }

    val pages = mutableListOf<Component>()

    val placeholders = mutableMapOf(
        "player" to target.name,
    )

    val punishmentChunks = punishmentsList.chunked(8)
    punishmentChunks.forEachIndexed { index, chunk ->
        val punishments = chunk.joinToString("<newline>")
        val newPlaceholders = placeholders + mapOf(
            "punishments" to punishments,
            "current_page" to index + 1,
            "pages" to punishmentChunks.size,
        )

        val pageText = messageUtil
            .translate("punishments.commands.punish.initial", newPlaceholders)
            .trimIndent()
        pages.add(pageText.mm())
    }

    val book = Book.book(Component.empty(), "Azalea Team".mm(), pages)
    (Bukkit.getPlayer(player.uuid) ?: return).openBook(book)
}

/*
    Gets the reason string for a punishment.

    Reduced version of PunishmentData.getReasonString(),
    since the punishment is not in the database yet.
 */
@Ignore
fun PunishmentConfig.getReasonString(): String {
    var text: String
    val reason = this.shortReason

    text = when (this.action) {
        PunishmentTypes.BAN.name, PunishmentTypes.AUTO_BAN.name -> "<red>$reason</red>"
        else -> "<blue>$reason</blue>"
    }
    if (text.isEmpty()) text = reason

    return text
}

@Ignore
private fun confirmPunishment(player: User, target: User, short: String) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val info = punishmentConfig.punishments[short] ?: throw IllegalArgumentException("Invalid punishment: $short")
    val punishmentType = PunishmentTypes.valueOf(info.action.uppercase())

    val pages = mutableListOf<Component>()

    val placeholders = mapOf(
        "player" to target.name,
        "reason" to info.getReasonString(),
        "type" to punishmentType.name.lowercase(),
        "short" to short
    )

    val pageText = messageUtil
        .translate("punishments.commands.punish.confirm", placeholders)
        .trimIndent()
    pages.add(pageText.mm())

    val book = Book.book(Component.empty(), "Azalea Team".mm(), pages)
    (Bukkit.getPlayer(player.uuid) ?: return@launch).openBook(book)
}

@Ignore
private fun finishPunishment(user: User, target: User, short: String) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val info = punishmentConfig.punishments[short] ?: throw IllegalArgumentException("Invalid punishment: $short")
    val punishmentType = PunishmentTypes.valueOf(info.action.uppercase())

    @Ignore
    suspend fun getPunishmentDuration(): String {
        if(info.duration === null) return "FOREVER"

        val punishmentInstances = Punishments.list(target.uuid)
            .filter { short.equals(it.reason, true) }

        if(punishmentInstances.size >= info.duration.size) return info.duration.last()

        return info.duration[punishmentInstances.size]
    }

    try {
        Punishment(
            moderator = user,
            player = target,
            reason = short.uppercase(),
            type = punishmentType,
            duration = getPunishmentDuration(),
        ).handle()
        user.getPlayer().sendKey("punishments.commands.punish.success")
    } catch (e: Exception) {
        user.getPlayer().sendKey("punishments.commands.punish.error", "reason" to (e.message ?: "Unknown"))
    }
}

fun punish(
    player: Player, target: OfflinePlayer,
    @Optional short: PunishmentShort?, @Optional confirm: String?,
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val executorUser = User(player.uniqueId, player.name)
    val targetUser = User(target.uniqueId, target.name ?: "Unknown")

    val activePunishment = Punishments.list(target.uniqueId).find {
        it.active == 1
    }

    if(activePunishment !== null) {
        player.sendKey("punishments.commands.punish.hasActivePunishment", "id" to activePunishment.id)
        return@launch
    }

    if(short === null) {
        initialCommand(executorUser, targetUser)
        return@launch
    }

    if(isConfirmed(confirm)) {
        finishPunishment(executorUser, targetUser, short.toString())
        return@launch
    }

    confirmPunishment(executorUser, targetUser, short.toString())
}