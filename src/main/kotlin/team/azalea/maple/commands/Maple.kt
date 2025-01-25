@file:Command("maple", permission = "maple.staff.maple")

package team.azalea.maple.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import gg.ingot.iron.bindings.bind
import me.honkling.commando.common.annotations.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import team.azalea.maple.Database
import team.azalea.maple.discord.Discord
import team.azalea.maple.filter.chatFilterInstance
import team.azalea.maple.maplePlugin
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
    Discord.reloadConfig()
    chatFilterInstance.reloadFilterConfiguration()

    player.sendKey("commands.maple.reload")
}

fun setServerName(player: CommandSender, name: String) = maplePlugin.launch (maplePlugin.asyncDispatcher) {
    if(!player.isOp) return@launch
    
    val iron = Database.getIron()
    iron.prepare("""
        INSERT OR REPLACE INTO server_settings (key, value) VALUES (:key, :value)
    """.trimIndent(), bind {
        "key" to "server_name"
        "value" to name
    })

    player.sendMessage("Server name set to $name")
}

fun setMinehutInfo(player: CommandSender, name: String, id: String) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    if (!player.isOp) return@launch

    val database = Database.getIron()

    try {
        suspend fun setServerSetting(key: String, value: String) {
            database.prepare(" INSERT OR REPLACE INTO server_settings (key, value) VALUES (:key, :value)", bind {
                "key" to key
                "value" to value
            })
        }

        setServerSetting("minehut_name", name)
        setServerSetting("minehut_id", id)

        player.sendMessage("Server ID set to $id, server name set to $name")
    } catch (e: Exception) {
        player.sendMessage("An error occurred while updating server settings.")
        e.printStackTrace()
    }
}
