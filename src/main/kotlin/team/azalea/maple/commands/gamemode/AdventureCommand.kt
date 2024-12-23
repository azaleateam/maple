@file:Command("gma", permission = "maple.staff.gamemode.adventure")

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player

fun gma(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == GameMode.ADVENTURE) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendMessage("You're already in adventure mode!")
            else -> player.sendMessage("${targetPlayer.name} is already in adventure mode!")
        }
        return
    }

    targetPlayer.gameMode = GameMode.ADVENTURE
    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendMessage("You've switched to adventure mode!")
        else -> player.sendMessage("${targetPlayer.name} has switched to adventure mode!")
    }
}