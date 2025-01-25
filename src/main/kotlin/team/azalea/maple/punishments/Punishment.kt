package team.azalea.maple.punishments

/**
 *  This file manages everything related to punishments.
 *  It also functions as an SDK for punishment data
 */

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import gg.ingot.iron.annotations.Column
import gg.ingot.iron.annotations.Model
import gg.ingot.iron.bindings.Bindings
import gg.ingot.iron.strategies.NamingStrategy
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import team.azalea.maple.Database
import team.azalea.maple.discord.discordConfig
import team.azalea.maple.discord.useBot
import team.azalea.maple.maplePlugin
import team.azalea.maple.messageUtil
import team.azalea.maple.util.mm
import team.azalea.maple.util.replacePlaceholders
import java.awt.Color
import java.time.Instant
import java.util.UUID

/**
 *  Enum representing the different types of punishments.
 */
enum class PunishmentTypes {
    MUTE,
    KICK,
    BAN,
    AUTO_BAN;

    /**
     *  Checks if the punishment type is a ban.
     */
    fun isBan(): Boolean {
        return this == BAN || this == AUTO_BAN
    }

    /**
     *  Gets the string representation of the punishment type.
     */
    override fun toString(): String {
        return if (this == AUTO_BAN) "automatically ban" else super.toString()
    }

    companion object {
        /**
         *  Checks if the punishment type is a ban.
         *
         *  @param type The ordinal value of the punishment type
         *  @return True if the punishment type is a ban, false otherwise
         */
        fun isBan(type: Int): Boolean {
            return entries[type].isBan()
        }
    }
}

private fun getNumericValue(input: String): Pair<Int, String> {
    val regex = Regex("(\\d+)(\\D+)")
    val matchResult = regex.find(input)
    val (numericValue, timeUnit) = matchResult?.destructured ?: throw IllegalArgumentException("Invalid input format: $input")
    return Pair(numericValue.toInt(), timeUnit)
}

/**
 * Converts a duration string to seconds.
 *
 * @param input Duration string (e.g., "1h")
 * @return Duration in seconds
 */
fun convertDate(input: String): Long {
    if (input.lowercase() == "forever") return Instant.MAX.epochSecond

    val (numericValue, timeUnit) = getNumericValue(input)
    val timeUnits = timeUnits()

    val secondValue = timeUnits[timeUnit]
        ?: throw IllegalArgumentException("Invalid time unit specified: $timeUnit")

    return numericValue * secondValue
}

/**
 * Provides a map of time unit labels to their corresponding values in seconds.
 *
 * @return Map of time unit labels to seconds
 */
fun timeUnits(): Map<String, Long> {
    val minute = 60L
    val hour = minute * 60
    val day = hour * 24
    val week = day * 7
    val month = week * 4

    return mapOf(
        "min" to minute,
        "h" to hour,
        "d" to day,
        "w" to week,
        "mo" to month
    )
}

/**
 *  Converts a string to its plural form.
 *
 *  example: "kick" -> "kicked"
 *  example: "ban" -> "banned"
 */
private fun String.toPluralForm(): String {
    return when {
        endsWith("n") -> this + "ned"
        endsWith("e") -> this + "d"
        else -> this + "ed"
    }
}

/**
 *  A representation of a user.
 *
 *  This class is used to represent a user when creating a punishment.
 */
data class User(val uuid: UUID, val name: String) {
    fun getPlayer(): Player {
        return Bukkit.getPlayer(uuid) ?: throw IllegalStateException("Player $uuid is not online")
    }
}

val CONSOLE_USER = User(UUID(0, 0), "Console")

/**
 *  A representation of a punishment within the database.
 */
@Model(table = "punishments", naming = NamingStrategy.SNAKE_CASE)
data class PunishmentData(
    @Column(primaryKey = true)
    /** The ID of the punishment */
    val id: String = UUID.randomUUID().toString(),
    /** The UUID of the moderator */
    val moderator: String,
    /** The UUID of the player being punished */
    val player: String,
    /** The reason for the punishment */
    var reason: String,
    /**
     * The type of punishment, an ordinal value of the [PunishmentTypes] enum.
     * */
    val type: Int,
    /** The timestamp of when the punishment was created */
    val createdAt: Long = Instant.now().epochSecond,
    /** The timestamp of when the punishment was last updated */
    val updatedAt: Long = createdAt,
    /** The duration of the punishment */
    var duration: Long,
    /** Whether the punishment is active, this is an int because SQLite doesn't support booleans */
    var active: Int = 1,
    /** Any notes attached to the punishment */
    var notes: String? = null,
    /** The UUID of the user who reverted the punishment, if applicable */
    val revertedBy: String? = null,
    /** The timestamp of when the punishment was reverted, if applicable */
    val revertedAt: Long? = null,
    /** The reason for the reversion, if applicable */
    val revertedReason: String? = null,
): Bindings {
    /**
     *  Converts the data to a punishment object.
     *
     *  @return A Punishment object
     */
    fun getClass(): Punishment {
        val moderatorUUID = UUID.fromString(this.moderator)
        val playerUUID = UUID.fromString(this.player)

        val moderator = User(moderatorUUID, Bukkit.getOfflinePlayer(moderatorUUID).name ?: "Console")
        val target = User(playerUUID, Bukkit.getOfflinePlayer(playerUUID).name ?: "Unknown")

        val duration = secondsToDuration(this.duration)

        return Punishment(
            moderator = moderator,
            player = target,
            reason = this.reason,
            type = PunishmentTypes.entries[this.type],
            duration = duration,
            notes = this.notes ?: "",
        )
    }

    /**
     *  Gets the created at timestamp of the punishment.
     *
     *  @return The created at timestamp
     */
    fun getCreatedAt(): Instant {
        return Instant.ofEpochSecond(this.createdAt)
    }

    /**
     *  Gets the updated at timestamp of the punishment.
     *
     *  @return The updated at timestamp
     */
    fun getUpdatedAt(): Instant {
        return Instant.ofEpochSecond(this.updatedAt)
    }

    fun getPlayerUUID(): UUID {
        return UUID.fromString(this.player)
    }

    fun getReasonString(): String {
        var text = ""
        var reason = this.reason
        lateinit var punishmentInfo: PunishmentConfig

        // If the reason is a predefined punishment, get the short reason
        // (otherwise, it would just be the short ID)
        // if it's not a predefined punishment, just use the reason as is
        if(reason.lowercase() in punishmentConfig.punishments.keys) {
            punishmentInfo = punishmentConfig.punishments[reason.lowercase()]!!
            reason = punishmentInfo.shortReason
        }

        if(this.active == 1) {
            text = if (PunishmentTypes.isBan(this.type)) "<red>$reason</red>"
            else "<blue>$reason</blue>"
        }

        if (this.revertedAt != null) text = "(R) $reason"

        if(text.isEmpty()) text = reason

        return text
    }
}

/**
 *  Gets the reason information for a punishment from the config.
 *
 *  @param reason The reason for the punishment
 *  @return A pair of short and long reasons
 */
fun getReasonInfo(reason: String): Pair<String, String> {
    var shortReason = reason
    var longReason = reason

    if(reason.lowercase() in punishmentConfig.punishments.keys) {
        val punishmentInfo = punishmentConfig.punishments[reason.lowercase()]!!
        shortReason = punishmentInfo.shortReason.capitalize()
        longReason = punishmentInfo.longReason
    }

    return Pair(shortReason, longReason)
}


/**
 * Converts a duration in seconds to a human-readable duration string.
 *
 * @param seconds Duration in seconds (e.g., 3600)
 * @return Duration string (e.g., "1h")
 */
fun secondsToDuration(seconds: Long): String {
    if (seconds == Long.MAX_VALUE) return "forever"

    val timeUnits = timeUnits().entries.sortedByDescending { it.value }

    for ((unitLabel, unitSeconds) in timeUnits) {
        if (seconds >= unitSeconds) {
            val value = seconds / unitSeconds
            return "$value$unitLabel"
        }
    }

    return "${seconds}s"
}

/**
 *  Data class representing a punishment.
 *
 *  This class is responsible for handling the punishment logic.
 */
data class Punishment(
    /** The moderator who is applying the punishment */
    val moderator: User,
    /** The player being punished */
    val player: User,
    /** The reason for the punishment */
    val reason: String,
    /** The type of punishment to apply */
    val type: PunishmentTypes,
    /** Duration string (ex: "1h") */
    val duration: String = "FOREVER",
    /** Notes to be attached to the punishment */
    val notes: String = "",
) {
    /**
     *  Gets the disconnect message for the punishment.
     *
     *  @return A Component representing the disconnect message
     */
    fun getDisconnectMessage(): Component {
        val key = when (this.type) {
            PunishmentTypes.BAN -> "punishments.playerBanned"
            PunishmentTypes.AUTO_BAN -> "punishments.playerAutoBanned"
            else -> "punishments.playerKicked"
        }
        val translation = messageUtil.getString(key)
        val placeholders = this.getPlaceholders()
        return translation.replacePlaceholders(placeholders).trimIndent().mm()
    }

    /**
     *  Gets the placeholders for the punishment.
     *
     *  @return A map of placeholders
     */
    private fun getPlaceholders(): Map<String, String> {
        val (shortReason, longReason) = getReasonInfo(this.reason)

        val placeholders = mapOf(
            "moderator" to this.moderator.name,
            "player" to this.player.name,
            "action" to this.type.name,
            "type" to this.getPluralType(),
            "short_reason" to shortReason,
            "long_reason" to longReason,
            "duration" to this.getFormattedDuration(),
            "notes" to this.notes,
        )

        return placeholders
    }

    private suspend fun sendDiscordLog(id: String) {
        val serverName = Database.getServerName()
        maplePlugin.logger.info("Player ${player.name} was ${getPluralType()} by ${moderator.name} for $reason (Punishment ID: $id)")

        useBot {
            val discordLogChannel = it.getTextChannelById(discordConfig.channels.log.toLong())
                ?: throw Exception("Failed to find #punish-logs channel")

            val (shortReason) = getReasonInfo(this.reason)
            val info = this

            val logEmbed = EmbedBuilder().setTitle("${info.player.name} was ${getPluralType()}")
                .setColor(Color.decode("#ff6e6e"))
                .setThumbnail("https://skins.mcstats.com/body/side/${info.player.uuid}")
                .addField(MessageEmbed.Field("Reason", shortReason, true))
                .addField(MessageEmbed.Field("Moderator", info.moderator.name, true))
                .addField(MessageEmbed.Field("Punishment ID", id, false))
                .setFooter("Server: $serverName")

            if(info.notes.isNotEmpty()) {
                logEmbed.addField(MessageEmbed.Field("Notes", info.notes, true))
            }

            val minehutReportUrl = getMinehutReportLink(this, Instant.now())

            val messageCreate = MessageCreateBuilder()
                .setEmbeds(logEmbed.build())
                .addComponents(ActionRow.of(Button.link(minehutReportUrl, "Create Report")))
                .build()

            discordLogChannel.sendMessage(messageCreate).queue()
        }
    }

    /**
     *  Handles the punishment using the data provided.
     *
     *  @return The punishment object
     */
    suspend fun handle(): Punishment {
        var duration = convertDate(this.duration)
        if(this.type == PunishmentTypes.KICK) duration = 0

        val punishment = Punishments.create(
            moderator = this.moderator.uuid,
            player = this.player.uuid,
            reason = this.reason,
            type = this.type.ordinal,
            duration = duration,
            notes = this.notes,
            active = true,
        )

        val logPlaceholders = this.getPlaceholders()
            .plus(mapOf("id" to punishment.id))

        val key = if(this.type == PunishmentTypes.KICK) "punishments.kickLog" else "punishments.log"
        val logString = messageUtil.getString(key).replacePlaceholders(logPlaceholders).trimIndent()

        Bukkit.getOnlinePlayers().filter { it.hasPermission("maple.staff.punish") }
            .forEach {
                it.sendMessage(logString.mm())
            }

        sendDiscordLog(punishment.id)

        val onlinePlayer = maplePlugin.server.onlinePlayers.firstOrNull {
            it.uniqueId == player.uuid
        }

        if(onlinePlayer == null) {
            maplePlugin.logger.info("[Punishments] Player ${player.name} was not online, but punishment was applied.")
            return this
        }

        when(this.type) {
            PunishmentTypes.BAN,
            PunishmentTypes.AUTO_BAN,
            PunishmentTypes.KICK -> {
                withContext(maplePlugin.minecraftDispatcher) {
                    onlinePlayer.kick(getDisconnectMessage())
                }
            }
            PunishmentTypes.MUTE -> {
                val mutedMsg = messageUtil.getString("punishments.playerMuted")
                    .replacePlaceholders(logPlaceholders).trimIndent()
                onlinePlayer.sendMessage(mutedMsg.mm())
            }
        }

        return this
    }

    /**
     *  Fetches the punishment type in a human-readable format.
     *
     *  example: "kick" -> "kicked"
     */
    private fun getPluralType(): String {
        return this.type.toString().lowercase().toPluralForm()
    }

    /**
     *  Gets the formatted duration of the punishment.
     *
     *  Note: This is not the expiry of the punishment, but the overall duration of the punishment.
     *
     *  example: "1h" -> "1 hour"
     */
    fun getFormattedDuration(): String {
        println("Getting formatted duration for ${this.duration}")

        if(this.duration.lowercase() == "forever") return "forever"

        val (numericValue, timeUnit) = getNumericValue(this.duration)

        var unit = when(timeUnit) {
            "min" -> "minute"
            "h" -> "hour"
            "d" -> "day"
            "w" -> "week"
            "mo" -> "month"
            else -> throw Exception("Invalid time unit specified $timeUnit")
        }
        if(numericValue > 1) unit += "s"

        val duration = "$numericValue $unit"

        return duration
    }
}