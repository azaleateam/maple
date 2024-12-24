@file:Command("gma", permission = "maple.staff.gamemode.adventure")

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player
import team.azalea.maple.ext.sendKey

fun gma(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == GameMode.ADVENTURE) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendKey("commands.gamemode.alreadySelf", "adventure")
            else -> player.sendKey("commands.gamemode.alreadyOther", targetPlayer.name, "adventure")
        }
        return
    }

    targetPlayer.gameMode = GameMode.ADVENTURE
    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendKey("commands.gamemode.self", "adventure")
        else -> player.sendKey("commands.gamemode.other", targetPlayer.name, "adventure")
    }
}