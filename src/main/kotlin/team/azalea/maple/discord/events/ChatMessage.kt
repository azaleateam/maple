package team.azalea.maple.discord.events

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import team.azalea.maple.discord.discordConfig
import team.azalea.maple.maplePlugin
import team.azalea.maple.util.mm
import team.azalea.maple.util.replacePlaceholders

object ChatMessage: ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if(event.channel.id != discordConfig.channels.main) return
        if(event.author.isBot) return

        val configMsg = discordConfig.ingame.content
        val msgContent = event.message.contentDisplay

        val placeholders = mapOf(
            "display-name" to event.author.effectiveName,
            "name" to event.author.name,
            "message" to msgContent,
            "tag" to (event.author.globalName ?: ""),
        )
        val parsedMsg = configMsg.replacePlaceholders(placeholders)

        maplePlugin.server.broadcast(parsedMsg.mm())
    }
}