@file:Command(
    name = "kick",
    description = "Kicks a player",
    aliases = ["k"],
    usage = "kick <player> <reason> [notes]",
    permission = "maple.staff.punishments.kick",
)

package team.azalea.maple.punishments.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import me.honkling.commando.common.annotations.Command
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import team.azalea.maple.maplePlugin
import team.azalea.maple.punishments.*
import team.azalea.maple.util.sendKey

fun kick(
    player: CommandSender, target: OfflinePlayer,
    vararg reason: String,
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val executorUser = if(player is Player) User(player.uniqueId, player.name) else CONSOLE_USER
    val targetUser = User(target.uniqueId, target.name ?: "Unknown")

    try {
        Punishment(
            moderator = executorUser,
            player = targetUser,
            reason = reason.joinToString(" "),
            type = PunishmentTypes.KICK,
            notes = ""
        ).handle()
    } catch (e: Exception) {
        player.sendKey("punishments.commands.kick.error", "reason" to (e.message ?: "Unknown"))
    }
}