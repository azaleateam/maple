@file:Command("gms", permission = "maple.staff.gamemode.survival")

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player
import team.azalea.maple.ext.sendKey

fun gms(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == GameMode.SURVIVAL) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendKey("commands.gamemode.alreadySelf", "survival")
            else -> player.sendKey("commands.gamemode.alreadyOther", targetPlayer.name, "survival")
        }
        return
    }

    targetPlayer.gameMode = GameMode.SURVIVAL
    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendKey("commands.gamemode.self", "survival")
        else -> player.sendKey("commands.gamemode.other", targetPlayer.name, "survival")
    }
}