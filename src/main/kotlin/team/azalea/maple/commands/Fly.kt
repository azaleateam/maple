@file:Command("fly", permission = "maple.staff.fly")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.entity.Player
import team.azalea.maple.util.sendKey

fun fly(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player
    targetPlayer.allowFlight = !targetPlayer.allowFlight

    val text = if(targetPlayer.allowFlight) "Enabled" else "Disabled"

    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendKey("commands.fly.self", "status" to text)
        else -> player.sendKey("commands.fly.other", mapOf("status" to text, "target" to targetPlayer.name))
    }
}