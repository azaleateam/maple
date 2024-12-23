@file:Command("gamemode", permission = "maple.staff.gamemode", aliases = ["gm"])

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player

fun gamemode(player: Player, mode: GameMode, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == mode) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendMessage("You're already in ${mode.name.lowercase()} mode!")
            else -> player.sendMessage("${targetPlayer.name} is already in ${mode.name.lowercase()} mode!")
        }
        return
    }

    when (mode) {
        GameMode.CREATIVE -> targetPlayer.gameMode = GameMode.CREATIVE
        GameMode.SURVIVAL -> targetPlayer.gameMode = GameMode.SURVIVAL
        GameMode.ADVENTURE -> targetPlayer.gameMode = GameMode.ADVENTURE
        GameMode.SPECTATOR -> targetPlayer.gameMode = GameMode.SPECTATOR
    }

    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendMessage("You've switched to ${mode.name.lowercase()} mode!")
        else -> player.sendMessage("${targetPlayer.name} has switched to ${mode.name.lowercase()} mode!")
    }
}