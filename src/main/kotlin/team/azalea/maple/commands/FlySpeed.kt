@file:Command("flyspeed", permission = "maple.staff.flyspeed")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.entity.Player
import team.azalea.maple.util.sendKey

fun flyspeed(player: Player, @Optional speed: Float, @Optional target: Player?) {
    val targetPlayer = target ?: player

    val placeholders = mapOf(
        "speed" to speed.toString(),
        "target" to targetPlayer.name
    )

    if (speed < -10 || speed > 10) {
        player.sendKey("commands.flyspeed.invalid", placeholders)
        return
    }

    targetPlayer.flySpeed = speed / 10

    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendKey("commands.flyspeed.self", placeholders)
        else -> player.sendKey("commands.flyspeed.other", placeholders)
    }
}