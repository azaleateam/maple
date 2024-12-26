@file:Command(
    name = "spawn",
    description = "Teleports you to the spawn location",
    usage = "spawn",
)

package team.azalea.maple.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import me.honkling.commando.common.annotations.Command
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import team.azalea.maple.Database
import team.azalea.maple.maplePlugin
import team.azalea.maple.models.ServerSettings

fun spawn(player: Player) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val iron = Database.getIron()

    val spawnLocation = iron.prepare("""
        SELECT * FROM server_settings WHERE key = 'spawn_location'
    """.trimIndent()).singleNullable<ServerSettings>()
        ?: throw IllegalStateException("No spawn location found")

    val serializedSpawn = spawnLocation.value.split(", ").let {
        val x = it[0].toDouble()
        val y = it[1].toDouble()
        val z = it[2].toDouble()
        val world = Bukkit.getWorld(it[3])
        val yaw = it[4].toFloat()
        val pitch = it[5].toFloat()
        Location(world, x, y, z, yaw, pitch)
    }


    maplePlugin.launch(maplePlugin.minecraftDispatcher) {
        player.teleport(serializedSpawn)
    }
}