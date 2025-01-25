@file:Command(
    name = "broadcast",
    description = "Broadcast a message to all players",
    aliases = ["broadcasts", "bc"],
    usage = "broadcast <message>",
    permission = "maple.staff.broadcast",
)

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import team.azalea.maple.util.mm

fun broadcast(player: CommandSender, vararg message: String) = Bukkit.broadcast(message.joinToString(" ").mm())