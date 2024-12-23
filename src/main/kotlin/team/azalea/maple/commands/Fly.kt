@file:Command("fly", permission = "maple.staff.fly")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.entity.Player

fun fly(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player
    targetPlayer.allowFlight = true

    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendMessage("You're now able to fly!")
        else -> player.sendMessage("${targetPlayer.name} is now able to fly!")
    }
}