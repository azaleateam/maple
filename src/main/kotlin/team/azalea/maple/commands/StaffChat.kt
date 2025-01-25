@file:Command("staffchat", permission = "maple.staff", aliases = ["sc"])

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import team.azalea.maple.messageUtil
import team.azalea.maple.util.mm
import team.azalea.maple.util.replacePlaceholders

fun staffChat(player: Player, vararg message: String) {
    val msgFormat = messageUtil.getString("staffchat.message")
    val msg = msgFormat.replacePlaceholders(mapOf(
        "message" to message.joinToString(" "),
        "player" to player.name,
    ))

    Bukkit.getOnlinePlayers().filter { it.hasPermission("maple.staff") }
        .forEach {
            it.sendMessage(msg.mm())
        }
}