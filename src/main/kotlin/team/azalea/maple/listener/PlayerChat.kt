package team.azalea.maple.listener

import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import team.azalea.maple.Database
import team.azalea.maple.discord.discordConfig
import team.azalea.maple.discord.useBot
import team.azalea.maple.filter.FilterAction
import team.azalea.maple.filter.chatFilterInstance
import team.azalea.maple.maplePlugin
import team.azalea.maple.punishments.*
import team.azalea.maple.util.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PlayerChatListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event: AsyncChatEvent) = runBlocking {
        if(InputHandler.isUsingUnfiltered(event.player)) {
            maplePlugin.logger.info("Player ${event.player.name} is using an unfiltered chat input, input will be ignored.")
            return@runBlocking
        }

        val player = event.player
        val name = player.name().plainText()
        val message = event.message().plainText()

        val activeMute = Punishments.list(player.uniqueId).find {
            it.active == 1 && it.type == PunishmentTypes.MUTE.ordinal
        }

        if(activeMute !== null) {
            event.isCancelled = true

            val reasonInfo = getReasonInfo(activeMute.reason)

            val placeholders = mapOf(
                "short_reason" to reasonInfo.first,
                "full_reason" to reasonInfo.second,
                "duration" to activeMute.getClass().getFormattedDuration(),
            )

            player.sendKey(
                "punishments.playerMuted",
                placeholders
            )

            return@runBlocking
        }

        val filterResult = chatFilterInstance.validateMessage(player, message)
        val filterRuleset = filterResult.ruleset
        val filterAction = filterRuleset?.action

        if (filterAction == FilterAction.BLOCK || filterAction == FilterAction.BAN) {
            event.isCancelled = true
            val serverName = Database.getServerName()

            maplePlugin.logger.info("$name triggered the filter with message: $message")

            val underlinedMessage = message.replace(
                filterResult.failedTokens.first().value,
                "<u><#ff6e6e>${filterResult.failedTokens.first().value}</#ff6e6e></u>"
            )
            Bukkit.getOnlinePlayers().filter { it.hasPermission("maple.staff.chatFilter") }
                .forEach {
                    it.sendKey(
                        "chatFilter.message",
                        mapOf(
                            "username" to name,
                            "message" to underlinedMessage,
                            "action" to filterRuleset.action.name.lowercase(),
                        )
                    )
                }

            if (filterRuleset.action == FilterAction.BAN) {
                Punishment(
                    CONSOLE_USER,
                    User(player.uniqueId, name),
                    filterRuleset.banReason,
                    PunishmentTypes.AUTO_BAN
                ).handle()
            }

            useBot {
                it.getTextChannelById(chatFilterInstance.logChannel ?: "0")?.let { logChannel ->
                    val firstToken = filterResult.failedTokens.first()

                    val formattedMessage = message
                        .replace("`", "\\`")
                        .replaceFirst(firstToken.value, "`>>>${firstToken.value}<<<`")

                    val logEmbed = EmbedBuilder()
                        .setTitle("`${player.name}` triggered the filter")
                        .setColor(java.awt.Color.decode("#ff6e6e"))
                        .addField(MessageEmbed.Field("Message", formattedMessage, false))
                        .addField(MessageEmbed.Field("Action", filterAction.toString(), false))
                        .setFooter("Path: ${filterResult.ruleset.path.substringAfterLast('/')} (Priority: ${filterResult.ruleset.priority}) [Env: $serverName]")
                        .build()

                    logChannel.sendMessageEmbeds(logEmbed).queue()
                }
            }

        }

        useBot {
            if(event.isCancelled) return@useBot

            val configMsg = discordConfig.chat.content
            val channel = it.getTextChannelById(discordConfig.channels.main.toLong())
                ?: throw Exception("Failed to find guild!")

            val timestamp = DateTimeFormatter.ofPattern("HH:mm:ss z").format(ZonedDateTime.now())

            val user = playerAdapter.getUser(player)
            val playerRank = user.cachedData.metaData.prefix
                .orEmpty().mm().plainText().trim()

            val parsedMsg = configMsg.replacePlaceholders(mapOf(
                "name" to name,
                "rank" to playerRank,
                "message" to message,
                "timestamp" to timestamp,
            ))

            channel.sendMessage(parsedMsg).queue()
        }
    }
}

