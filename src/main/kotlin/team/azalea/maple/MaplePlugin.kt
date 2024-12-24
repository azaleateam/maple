package team.azalea.maple

import me.honkling.commando.spigot.SpigotCommandManager
import me.honkling.commando.spigot.SpigotListenerManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.plugin.java.JavaPlugin
import team.azalea.maple.types.GamemodeType
import team.azalea.maple.util.MapleMiniMessage
import team.azalea.maple.util.MessageUtil

val maplePlugin = JavaPlugin.getPlugin(MaplePlugin::class.java)

/* Global message util instance */
lateinit var messageUtil: MessageUtil

/* Global minimessage instance, reliant on messageUtil */
lateinit var miniMessage: MiniMessage

class MaplePlugin : JavaPlugin() {
    override fun onEnable() {
        val listenerManager = SpigotListenerManager(this)
        listenerManager.registerListeners("team.azalea.maple.listener")

        val commandManager = SpigotCommandManager(this)
        registerTypes(commandManager)
        commandManager.registerCommands("team.azalea.maple.command")

        messageUtil = MessageUtil.initialize()
        miniMessage = MapleMiniMessage().build()

        logger.info("Maple has been enabled! 🍁")
    }

    private fun registerTypes(manager: SpigotCommandManager) {
        manager.types[GameMode::class.java] = GamemodeType
    }
}
