@file:Command("gmc", permission = "maple.staff.gamemode.creative")

package team.azalea.maple.commands.gamemode

import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Optional
import org.bukkit.GameMode
import org.bukkit.entity.Player
import team.azalea.maple.util.sendKey

fun gmc(player: Player, @Optional target: Player?) {
    val targetPlayer = target ?: player

    val placeholders = mapOf(
        "target" to targetPlayer.name,
        "gamemode" to "creative"
    )

    if (targetPlayer.gameMode == GameMode.CREATIVE) {
        when (targetPlayer.uniqueId) {
            player.uniqueId -> player.sendKey("commands.gamemode.alreadySelf", placeholders)
            else -> player.sendKey("commands.gamemode.alreadyOther", placeholders)
        }
        return
    }

    targetPlayer.gameMode = GameMode.CREATIVE
    when (targetPlayer.uniqueId) {
        player.uniqueId -> player.sendKey("commands.gamemode.self", placeholders)
        else -> player.sendKey("commands.gamemode.other", placeholders)
    }
}