@file:Command("maple", permission = "maple.staff.maple")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import team.azalea.maple.messageUtil
import team.azalea.maple.miniMessage
import team.azalea.maple.util.MapleMiniMessage
import team.azalea.maple.util.MessageUtil
import team.azalea.maple.util.sendKey

fun maple(player: Player) {
    player.sendKey("commands.maple.base")
}

fun reload(player: CommandSender) {
    messageUtil = MessageUtil.initialize()
    miniMessage = MapleMiniMessage().build()

    player.sendKey("commands.maple.reload")
}