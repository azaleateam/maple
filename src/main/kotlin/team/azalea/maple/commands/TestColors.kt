@file:Command("testcolors", permission = "maple.staff.testcolors")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import org.bukkit.entity.Player
import team.azalea.maple.util.mm

fun testColors(player: Player, vararg message: String) {
    player.sendMessage(message.joinToString(" ").mm())
}