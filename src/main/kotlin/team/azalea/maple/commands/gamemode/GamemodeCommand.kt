@file:Command("gamemode", permission = "maple.staff.gamemode", aliases = ["gm"])

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player
import team.azalea.maple.ext.sendKey

fun gamemode(player: Player, mode: GameMode, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == mode) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendKey("commands.gamemode.alreadySelf", mode.name.lowercase())
            else -> player.sendKey("commands.gamemode.alreadyOther", targetPlayer.name, mode.name.lowercase())
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
        player.uniqueId -> player.sendKey("commands.gamemode.self", mode.name.lowercase())
        else -> player.sendKey("commands.gamemode.other", targetPlayer.name, mode.name.lowercase())
    }
}