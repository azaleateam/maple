@file:Command("gmc", permission = "maple.staff.gamemode.creative")

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player

fun gmc(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == GameMode.CREATIVE) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendMessage("You're already in creative mode!")
            else -> player.sendMessage("${targetPlayer.name} is already in creative mode!")
        }
        return
    }

    targetPlayer.gameMode = GameMode.CREATIVE
    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendMessage("You've switched to creative mode!")
        else -> player.sendMessage("${targetPlayer.name} has switched to creative mode!")
    }
}