package team.azalea.maple

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import me.honkling.commando.spigot.SpigotCommandManager
import me.honkling.commando.spigot.SpigotListenerManager
import me.honkling.commonlib.CommonLib
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.plugin.java.JavaPlugin
import team.azalea.maple.listener.PlayerChatListener
import team.azalea.maple.punishments.Punishments
import team.azalea.maple.types.GamemodeType
import team.azalea.maple.util.MapleMiniMessage
import team.azalea.maple.util.MessageUtil

val maplePlugin = JavaPlugin.getPlugin(MaplePlugin::class.java)

/* Global message util instance */
lateinit var messageUtil: MessageUtil

/* Global minimessage instance, reliant on messageUtil */
lateinit var miniMessage: MiniMessage

lateinit var commandManager: SpigotCommandManager
lateinit var listenerManager: SpigotListenerManager

class MaplePlugin : SuspendingJavaPlugin() {
    override fun onEnable() {
        listenerManager = SpigotListenerManager(this)
        listenerManager.registerListeners("team.azalea.maple.listener")

        commandManager = SpigotCommandManager(this)
        registerTypes(commandManager)
        commandManager.registerCommands("team.azalea.maple.commands")

        messageUtil = MessageUtil.initialize()
        miniMessage = MapleMiniMessage().build()

        Punishments.setup()
        registerListeners()

        logger.info("Maple has been enabled! 🍁")

        CommonLib(this)
    }

    private fun registerTypes(manager: SpigotCommandManager) {
        manager.types[GameMode::class.java] = GamemodeType
    }

    /**
     * used for listeners that require changes to the event handler
     * as commando has hardcoded listeners
     */
    private fun registerListeners() {
        server.pluginManager.registerEvents(PlayerChatListener(), this)
    }
}
