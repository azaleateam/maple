@file:Command(
    name = "ban",
    description = "Bans a player",
    aliases = ["b"],
    usage = "ban <player> <reason>",
    permission = "maple.staff.ban",
)

package team.azalea.maple.punishments.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import me.honkling.commando.common.annotations.Command
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import team.azalea.maple.maplePlugin
import team.azalea.maple.punishments.Punishment
import team.azalea.maple.punishments.PunishmentTypes
import team.azalea.maple.punishments.User

fun ban(player: Player, target: OfflinePlayer, reason: String) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    player.sendMessage("woa...")

    val executorUser = User(player.uniqueId, player.name)
    val targetUser = User(target.uniqueId, target.name ?: "Unknown")

    Punishment(
        moderator = executorUser,
        player = targetUser,
        reason = reason,
        type = PunishmentTypes.BAN,
        duration = "1w",
        notes = ""
    ).handle()
}