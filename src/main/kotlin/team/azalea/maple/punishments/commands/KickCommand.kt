@file:Command(
    name = "kick",
    description = "Kicks a player",
    aliases = ["k"],
    usage = "kick <player> <reason> [notes]",
    permission = "maple.staff.kick",
)

package team.azalea.maple.punishments.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import team.azalea.maple.maplePlugin
import team.azalea.maple.punishments.*

fun kick(
    player: CommandSender, target: OfflinePlayer,
    reason: String, @Optional vararg notes: String?
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val executorUser = if(player is Player) User(player.uniqueId, player.name) else CONSOLE_USER
    val targetUser = User(target.uniqueId, target.name ?: "Unknown")

    Punishment(
        moderator = executorUser,
        player = targetUser,
        reason = reason,
        type = PunishmentTypes.KICK,
        notes = notes.joinToString(" "),
    ).handle()
}