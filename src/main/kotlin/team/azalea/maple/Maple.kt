package team.azalea.maple

import org.bukkit.plugin.java.JavaPlugin

class Maple : JavaPlugin() {

    override fun onEnable() {
        logger.info("Maple has been enabled! 🍁")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
