@file:Command("gmsp", permission = "maple.staff.gamemode.spectator")

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player
import team.azalea.maple.ext.sendKey

fun gmsp(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == GameMode.SPECTATOR) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendKey("commands.gamemode.alreadySelf", "spectator")
            else -> player.sendKey("commands.gamemode.alreadyOther", targetPlayer.name, "spectator")
        }
        return
    }

    targetPlayer.gameMode = GameMode.SPECTATOR
    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendKey("commands.gamemode.self", "spectator")
        else -> player.sendKey("commands.gamemode.other", targetPlayer.name, "spectator")
    }
}