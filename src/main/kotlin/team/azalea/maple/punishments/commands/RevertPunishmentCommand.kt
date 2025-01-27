@file:Command(
    name = "revertpunishment",
    description = "Reverts a punishment",
    aliases = ["revertpun"],
    usage = "<id>",
    permission = "maple.staff.punishments.revert",
)

package team.azalea.maple.punishments.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import me.honkling.commando.common.annotations.Command
import me.honkling.commando.common.annotations.Ignore
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import team.azalea.maple.Database
import team.azalea.maple.discord.discordConfig
import team.azalea.maple.discord.useBot
import team.azalea.maple.maplePlugin
import team.azalea.maple.messageUtil
import team.azalea.maple.punishments.*
import team.azalea.maple.util.*
import java.awt.Color

@Ignore
private fun sendLog(punishment: PunishmentData, moderator: User) {
    val target = Bukkit.getOfflinePlayer(punishment.getPlayerUUID())
    val (shortReason) = getReasonInfo(punishment.reason)
    val playerName = target.name ?: "Unknown"

    val logPlaceholders = mapOf(
        "moderator" to moderator.name,
        "player" to playerName,
        "reason" to shortReason,
        "id" to punishment.id,
    )

    val message = messageUtil.translate("punishments.commands.revert.log", logPlaceholders).trimIndent()

    Bukkit.getOnlinePlayers().filter { it.hasPermission("maple.staff.punish") }
        .forEach {
            it.sendMessage(message.mm())
        }

    useBot {
        val discordLogChannel = it.getTextChannelById(discordConfig.channels.filterLog.toLong())
            ?: throw Exception("Failed to find logs channel")

        maplePlugin.launch(maplePlugin.asyncDispatcher) {
            val serverName = Database.getServerName()

            val logEmbed = EmbedBuilder().setTitle("Punishment for $playerName reverted")
                .setColor(Color.decode("#ff6e6e"))
                .setThumbnail("https://crafatar.com/renders/head/${punishment.getPlayerUUID()}")
                .addField(MessageEmbed.Field("Reverted by", moderator.name, false))
                .addField(MessageEmbed.Field("Punishment ID", punishment.id, false))
                .setFooter("Server: $serverName")
                .build()

            discordLogChannel.sendMessageEmbeds(logEmbed).queue()
        }
    }
}

fun revertPunishment(
    player: Player, id: String
) = maplePlugin.launch(maplePlugin.asyncDispatcher) {
    val punishment = Punishments.get(id)

    if(punishment === null) {
        player.sendKey("punishments.invalidId", "id" to id)
        return@launch
    }

   player.sendKey("punishments.commands.revert.provideMessage")

    fetchInput(player) {
        maplePlugin.launch(maplePlugin.asyncDispatcher) inner@{
            val moderatorUser = User(player.uniqueId, player.name)
            try {
                Punishments.revert(id, moderatorUser, it)
            } catch (e: Exception) {
                player.sendKey("punishments.commands.revert.error", "reason" to (e.message ?: "Unknown error"))
                return@inner
            }
            player.sendKey("punishments.commands.revert.success")
            sendLog(punishment, moderatorUser)
        }
    }.prompt()
}