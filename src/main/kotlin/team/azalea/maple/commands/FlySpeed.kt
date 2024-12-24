@file:Command("flyspeed", permission = "maple.staff.flyspeed")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.entity.Player
import team.azalea.maple.ext.sendKey

fun flyspeed(player: Player, @Optional speed: Float, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (speed < -10 || speed > 10) {
        player.sendKey("commands.flyspeed.invalid")
        return
    }

    targetPlayer.flySpeed = speed / 10

    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendKey("commands.flyspeed.self", speed)
        else -> player.sendKey("commands.flyspeed.other", targetPlayer.name, speed)
    }
}