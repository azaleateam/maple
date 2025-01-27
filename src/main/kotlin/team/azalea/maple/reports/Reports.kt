package team.azalea.maple.reports

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.bukkit.entity.Player
import team.azalea.maple.Database
import team.azalea.maple.commandManager
import team.azalea.maple.discord.discordConfig
import team.azalea.maple.discord.useBot
import team.azalea.maple.maplePlugin
import java.awt.Color

data class ReportConfig(
    val reason: String,
)

data class ReportsConfig(
    val reports: Map<String, ReportConfig>
)

lateinit var reportConfig: ReportsConfig

object Reports {
    private val mapper = tomlMapper {}

    /**
     * Sets up the report module by loading the config then registering commands and listeners.
     */
    fun setup() {
        maplePlugin.dataFolder.mkdirs()
        val configFile = maplePlugin.dataFolder.resolve("reports.toml")

        if (!configFile.exists()) {
            maplePlugin.saveResource(configFile.name, false)
        }

        reportConfig = mapper.decode(configFile.readText())
        commandManager.registerCommands("team.azalea.maple.reports.commands")
    }

    suspend fun sendDiscordLog(player: Player, target: Player, reason: String) {
        val serverName = Database.getServerName()

        useBot {
            val discordLogChannel = it.getTextChannelById(discordConfig.channels.punishLog.toLong())
                ?: throw Exception("Failed to find #punish-logs channel")

            val logEmbed = EmbedBuilder().setTitle("${player.name} reported ${target.name}")
                .setColor(Color.decode("#ff6e6e"))
                .setThumbnail("https://skins.mcstats.com/body/side/${target.uniqueId}")
                .addField(MessageEmbed.Field("Reason", reason, true))
                .setFooter("Server: $serverName")

            val messageCreate = MessageCreateBuilder()
                .setEmbeds(logEmbed.build())
                .build()

            discordLogChannel.sendMessage(messageCreate).queue()
        }
    }
}