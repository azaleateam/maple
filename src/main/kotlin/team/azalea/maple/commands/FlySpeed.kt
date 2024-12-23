@file:Command("flyspeed", permission = "maple.staff.flyspeed")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.entity.Player

fun flyspeed(player: Player, @Optional speed: Float, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (speed < -10 || speed > 10) {
        player.sendMessage("Invalid speed! Please use a value between -10 and 10.")
        return
    }

    targetPlayer.flySpeed = speed / 10

    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendMessage("You've set your fly speed to $speed!")
        else -> player.sendMessage("You've set the fly speed of ${targetPlayer.name} to $speed!")
    }
}