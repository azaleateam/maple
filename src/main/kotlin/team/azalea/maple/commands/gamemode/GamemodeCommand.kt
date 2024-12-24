@file:Command("gamemode", permission = "maple.staff.gamemode", aliases = ["gm"])

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player
import team.azalea.maple.util.sendKey

fun gamemode(player: Player, mode: GameMode, @Optional target: Player?) {
    val targetPlayer = target ?: player

    val placeholders = mapOf(
        "target" to targetPlayer.name,
        "gamemode" to mode.name.lowercase()
    )

    if (targetPlayer.gameMode == mode) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendKey("commands.gamemode.alreadySelf", placeholders)
            else -> player.sendKey("commands.gamemode.alreadyOther", placeholders)
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
        player.uniqueId -> player.sendKey("commands.gamemode.self", placeholders)
        else -> player.sendKey("commands.gamemode.other", placeholders)
    }
}