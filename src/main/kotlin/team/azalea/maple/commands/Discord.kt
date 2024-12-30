@file:Command("discord")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import org.bukkit.entity.Player
import team.azalea.maple.util.sendKey

fun discord(player: Player) {
    player.sendKey("commands.discord.base")
}