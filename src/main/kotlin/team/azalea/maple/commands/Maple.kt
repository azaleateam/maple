@file:Command("maple", permission = "maple.staff.maple")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import org.bukkit.entity.Player
import team.azalea.maple.Translations
import team.azalea.maple.ext.sendKey


fun maple(player: Player) {
    player.sendKey("commands.maple.base")
}

fun reload(player: Player) {
    Translations.reload()
    player.sendKey("commands.maple.reload")
}