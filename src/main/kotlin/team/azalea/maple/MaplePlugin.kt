package team.azalea.maple

import me.honkling.commando.spigot.SpigotCommandManager
import me.honkling.commando.spigot.SpigotListenerManager
import org.bukkit.GameMode
import org.bukkit.plugin.java.JavaPlugin
import team.azalea.maple.types.GamemodeType

val instance = JavaPlugin.getPlugin(MaplePlugin::class.java)

class MaplePlugin : JavaPlugin() {
    override fun onEnable() {
        val listenerManager = SpigotListenerManager(this)
        listenerManager.registerListeners("team.azalea.maple.listener")

        val commandManager = SpigotCommandManager(this)
        registerTypes(commandManager)
        commandManager.registerCommands("team.azalea.maple.command")

        logger.info("Maple has been enabled! 🍁")
    }


    private fun registerTypes(manager: SpigotCommandManager) {
        manager.types[GameMode::class.java] = GamemodeType
    }
}
