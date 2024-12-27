package team.azalea.maple.discord

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import team.azalea.maple.discord.events.ChatMessage
import team.azalea.maple.maplePlugin

internal lateinit var bot: JDA

data class DiscordConfig(
    val botToken: String,
    val channels: Channels,
    val join: MessageContent,
    val leave: MessageContent,
    val chat: MessageContent,
    val ingame: MessageContent,
)

data class MessageContent(
    val content: String
)

data class Channels(
    val main: String,
    val log: String
)

lateinit var discordConfig: DiscordConfig

object Discord {
    private val mapper = tomlMapper {}

    fun load() {
        maplePlugin.dataFolder.mkdirs()
        val configFile = maplePlugin.dataFolder.resolve("discord.toml")

        if (!configFile.exists()) {
            maplePlugin.saveResource(configFile.name, false)
        }

        discordConfig = mapper.decode(configFile.readText())

        if(discordConfig.botToken.isEmpty()) {
            maplePlugin.logger.warning("Discord bot token is empty! Discord functionality will not be available.")
            return
        }

        bot = JDABuilder.createDefault(discordConfig.botToken)
            .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .addEventListeners(ChatMessage)
            .build()
            .awaitStatus(JDA.Status.INITIALIZED)

        // Attach command handler
        bot.updateCommands().complete()

        // Load all members
        bot.guilds.forEach {
            it.loadMembers()
        }
    }

    fun reloadConfig() {
        discordConfig = mapper.decode(maplePlugin.dataFolder.resolve("discord.toml").readText())
    }

    fun isConnected() = bot.status == JDA.Status.CONNECTED
}