@file:Command("fly", permission = "maple.staff.fly")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.entity.Player
import team.azalea.maple.util.sendKey

fun fly(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player
    targetPlayer.allowFlight = true

    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendKey("commands.fly.self")
        else -> player.sendKey("commands.fly.other", "target" to targetPlayer.name)
    }
}