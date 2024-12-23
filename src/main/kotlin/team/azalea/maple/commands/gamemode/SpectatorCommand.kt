@file:Command("gmsp", permission = "maple.staff.gamemode.spectator")

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player

fun gmsp(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == GameMode.SPECTATOR) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendMessage("You're already in spectator mode!")
            else -> player.sendMessage("${targetPlayer.name} is already in spectator mode!")
        }
        return
    }

    targetPlayer.gameMode = GameMode.SPECTATOR
    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendMessage("You've switched to spectator mode!")
        else -> player.sendMessage("${targetPlayer.name} has switched to spectator mode!")
    }
}