package team.azalea.maple

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import me.honkling.commando.spigot.SpigotCommandManager
import me.honkling.commando.spigot.SpigotListenerManager
import me.honkling.commonlib.CommonLib
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.plugin.java.JavaPlugin
import team.azalea.maple.discord.Discord
import team.azalea.maple.listener.PlayerChatListener
import team.azalea.maple.punishments.Punishments
import team.azalea.maple.reports.Reports
import team.azalea.maple.types.GamemodeType
import team.azalea.maple.util.*

val maplePlugin = JavaPlugin.getPlugin(MaplePlugin::class.java)

/* Global message util instance */
lateinit var messageUtil: MessageUtil

/* Global minimessage instance, reliant on messageUtil */
lateinit var miniMessage: MiniMessage

lateinit var commandManager: SpigotCommandManager
lateinit var listenerManager: SpigotListenerManager

class MaplePlugin : SuspendingJavaPlugin() {
    override fun onEnable() {
        Database.migrate()

        listenerManager = SpigotListenerManager(this)
        listenerManager.registerListeners("team.azalea.maple.listener")

        commandManager = SpigotCommandManager(this)
        registerTypes(commandManager)
        commandManager.registerCommands("team.azalea.maple.commands")

        messageUtil = MessageUtil.initialize()
        miniMessage = MapleMiniMessage().build()

        Punishments.setup()
        Reports.setup()
        Discord.load()
        registerListeners()
        startBroadcasts()

        logger.info("Maple has been enabled! 🍁")

        CommonLib(this)
    }

    private fun startBroadcasts() {
        println("Starting broadcasts")
        var currentIndex = 0

        infinity({
            val broadcasts = messageUtil.getList("broadcasts.all")
            var message = broadcasts.getOrNull(currentIndex) ?: run {
                currentIndex = 0
                broadcasts[currentIndex]
            }

            currentIndex++
            message = message.fixString().trimIndent()
            val sound = Sound.sound(
                Key.key("block.note_block.pling"),
                Sound.Source.AMBIENT,
                1f,
                1f
            )

            Bukkit.getOnlinePlayers().forEach {
                it.playSound(sound)
                it.sendMessage(message.replacePlaceholders(emptyMap()).mm())
            }
        }, 20 * 60 * 2)
    }

    private fun registerTypes(manager: SpigotCommandManager) {
        manager.types[GameMode::class.java] = GamemodeType
    }

    /**
     * used for listeners that require changes to the event handler
     * as commando has hardcoded listeners
     */

    /**
     * used for listeners that require changes to the event handler
     * as commando has hardcoded listeners
     */
    private fun registerListeners() {
        server.pluginManager.registerEvents(PlayerChatListener(), this)
    }
}
