@file:Command(
    name = "revertpunishment",
    description = "Reverts a punishment",
    aliases = ["revertpun"],
    usage = "<id>",
    permission = "maple.staff.punishments.revert",
)

package team.azalea.maple.punishments.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import me.honkling.commando.common.annotations.Command
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import team.azalea.maple.maplePlugin
import team.azalea.maple.messageUtil
import team.azalea.maple.punishments.*
import team.azalea.maple.util.*

private fun sendLog(punishment: PunishmentData, moderator: User) {
    val target = Bukkit.getOfflinePlayer(punishment.getPlayerUUID())
    val (shortReason) = getReasonInfo(punishment.reason)

    val logPlaceholders = mapOf(
        "moderator" to moderator.name,
        "player" to (target.name ?: "Unknown"),
        "reason" to shortReason,
        "id" to punishment.id,
    )

    val message = messageUtil.translate("punishments.commands.revert.log", logPlaceholders).trimIndent()

    Bukkit.getOnlinePlayers().filter { it.hasPermission("maple.staff.punish") }
        .forEach {
            it.sendMessage(message.mm())
        }
}

fun revertPunishment(
    player: Player, id: String
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val punishment = Punishments.get(id)

    if(punishment === null) {
        player.sendKey("punishments.invalidId", "id" to id)
        return@launch
    }

   player.sendKey("punishments.commands.revert.provideMessage")

    fetchInput(player) {
        maplePlugin.launch(maplePlugin.asyncDispatcher) inner@{
            val moderatorUser = User(player.uniqueId, player.name)
            try {
                Punishments.revert(id, moderatorUser, it)
            } catch (e: Exception) {
                player.sendKey("punishments.commands.revert.error", "reason" to (e.message ?: "Unknown error"))
                return@inner
            }
            player.sendKey("punishments.commands.revert.success")
            sendLog(punishment, moderatorUser)
        }
    }.prompt()
}