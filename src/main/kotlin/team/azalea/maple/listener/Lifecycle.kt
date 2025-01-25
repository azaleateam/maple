@file:Listener

package team.azalea.maple.listener

import me.honkling.commando.common.annotations.Listener
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import team.azalea.maple.discord.discordConfig
import team.azalea.maple.discord.useBot
import team.azalea.maple.util.mm
import team.azalea.maple.util.plainText
import team.azalea.maple.util.playerAdapter
import team.azalea.maple.util.replacePlaceholders
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private enum class EventType {
    JOIN,
    LEAVE,
}

private fun processEvent(player: Player, eventType: EventType) {
    val name = player.name().plainText()
    player.performCommand("spawn")

    useBot {
        val configMsg = when (eventType) {
            EventType.JOIN -> discordConfig.join.content
            EventType.LEAVE -> discordConfig.leave.content
        }

        val channel = it.getTextChannelById(discordConfig.channels.main.toLong())
            ?: throw Exception("Failed to find guild!")

        val timestamp = DateTimeFormatter.ofPattern("HH:mm:ss z").format(ZonedDateTime.now())

        val user = playerAdapter.getUser(player)
        val playerRank = user.cachedData.metaData.prefix
            .orEmpty().mm().plainText().trim()

        val parsedMsg = configMsg.replacePlaceholders(mapOf(
            "name" to name,
            "rank" to playerRank,
            "timestamp" to timestamp,
        ))

        channel.sendMessage(parsedMsg).queue()
    }
}

fun playerJoin(event: PlayerJoinEvent) {
    val player = event.player
    player.performCommand("spawn")
    processEvent(player, EventType.JOIN)
}

fun playerLeave(event: PlayerQuitEvent) {
    val player = event.player
    processEvent(player, EventType.LEAVE)
}
