@file:Command(
    name = "ban",
    description = "Bans a player",
    aliases = ["b"],
    usage = "ban <player> <reason> <duration> [notes]",
    permission = "maple.staff.punishments.ban",
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
import team.azalea.maple.util.sendKey

fun ban(
    player: CommandSender, target: OfflinePlayer, reason: String,
    duration: String, @Optional vararg notes: String?
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val executorUser = if(player is Player) User(player.uniqueId, player.name) else CONSOLE_USER
    val targetUser = User(target.uniqueId, target.name ?: "Unknown")

    val activeBan = Punishments.list(target.uniqueId).find {
        it.active == 1 && PunishmentTypes.isBan(it.type)
    }

    if(activeBan !== null) {
        player.sendKey("punishments.commands.ban.alreadyBanned", "target" to targetUser.name)
        return@launch
    }

    Punishment(
        moderator = executorUser,
        player = targetUser,
        reason = reason,
        type = PunishmentTypes.BAN,
        duration = duration,
        notes = notes.joinToString(" "),
    ).handle()
}