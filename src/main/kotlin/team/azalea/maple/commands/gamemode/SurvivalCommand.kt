@file:Command("gms", permission = "maple.staff.gamemode.survival")

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player

fun gms(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == GameMode.SURVIVAL) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendMessage("You're already in survival mode!")
            else -> player.sendMessage("${targetPlayer.name} is already in survival mode!")
        }
        return
    }

    targetPlayer.gameMode = GameMode.SURVIVAL
    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendMessage("You've switched to survival mode!")
        else -> player.sendMessage("${targetPlayer.name} has switched to survival mode!")
    }
}