@file:Command(
    name = "setspawn",
    description = "Sets the spawn location",
    usage = "setspawn",
    permission = "maple.staff.setSpawn",
)

package team.azalea.maple.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import gg.ingot.iron.bindings.bind
import me.honkling.commando.common.annotations.Command
import org.bukkit.entity.Player
import team.azalea.maple.Database
import team.azalea.maple.maplePlugin
import team.azalea.maple.util.sendKey

fun setSpawn(player: Player) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    player.sendKey("commands.setSpawn")

    val location = player.location
    val locString =  "${location.x}, ${location.y}, ${location.z}, ${location.world!!.name}, ${location.yaw}, ${location.pitch}"

    val iron = Database.getIron()
    iron.prepare("""
        INSERT INTO server_settings (key, value) VALUES (:key, :value)
    """.trimIndent(), bind {
        "key" to "spawn_location"
        "value" to locString
    })
}