@file:Command("gmc", permission = "maple.staff.gamemode.creative")

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player
import team.azalea.maple.ext.sendKey

fun gmc(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player

    if (targetPlayer.gameMode == GameMode.CREATIVE) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendKey("commands.gamemode.alreadySelf", "creative")
            else -> player.sendKey("commands.gamemode.alreadyOther", targetPlayer.name, "creative")
        }
        return
    }

    targetPlayer.gameMode = GameMode.CREATIVE
    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendKey("commands.gamemode.self", "creative")
        else -> player.sendKey("commands.gamemode.other", targetPlayer.name, "creative")
    }
}