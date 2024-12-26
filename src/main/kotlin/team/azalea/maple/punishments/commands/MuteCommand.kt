@file:Command(
    name = "mute",
    description = "Mutes a player",
    aliases = ["m"],
    usage = "mute <player> <reason> <duration> [notes]",
    permission = "maple.staff.punishments.mute",
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

fun mute(
    player: CommandSender, target: OfflinePlayer, reason: String,
    duration: String, @Optional vararg notes: String?
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val executorUser = if(player is Player) User(player.uniqueId, player.name) else CONSOLE_USER
    val targetUser = User(target.uniqueId, target.name ?: "Unknown")

    val activeMute = Punishments.list(target.uniqueId).find {
        it.active == 1 && it.type == PunishmentTypes.MUTE.ordinal
    }

    if(activeMute !== null) {
        player.sendKey("punishments.commands.mute.alreadyMuted", "target" to targetUser.name)
        return@launch
    }

      try {
          Punishment(
              moderator = executorUser,
              player = targetUser,
              reason = reason,
              type = PunishmentTypes.MUTE,
              duration = duration,
              notes = notes.joinToString(" "),
          ).handle()
      } catch (e: Exception) {
          player.sendKey("punishments.commands.mute.error", "reason" to (e.message ?: "Unknown"))
      }
}