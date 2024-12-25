package team.azalea.maple.punishments

/**
 *  This file manages everything related to punishments.
 *  It also functions as an SDK for punishment data
 */

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import gg.ingot.iron.annotations.Column
import gg.ingot.iron.annotations.Model
import gg.ingot.iron.bindings.Bindings
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import team.azalea.maple.maplePlugin
import team.azalea.maple.messageUtil
import team.azalea.maple.util.mm
import team.azalea.maple.util.replacePlaceholders
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
 *  Checks if a player can punish another player.
 *
 *  @param uuid The UUID of the player to check
 *  @return True if the player can punish, false otherwise
 */
fun canPunish(uuid: UUID): Boolean {
    return true
}

/**
 *  A representation of a user.
 *
 *  This class is used to represent a user when creating a punishment.//
 */
data class User(val uuid: UUID, val name: String)
val CONSOLE_USER = User(UUID(0, 0), "Console")

/**
 *  A representation of a punishment within the database.
 */
@Model
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
    @Column(name = "created_at")
    /** The timestamp of when the punishment was created */
    val createdAt: Long = Instant.now().epochSecond,
    @Column(name = "updated_at")
    /** The timestamp of when the punishment was last updated */
    val updatedAt: Long = createdAt,
    /** The duration of the punishment */
    var duration: Long,
    /** Whether the punishment is active, this is an int because SQLite doesn't support booleans */
    var active: Int = 1,
    /** Any notes attached to the punishment */
    var notes: String? = null,
    @Column(name = "reverted_by")
    /** The UUID of the user who reverted the punishment, if applicable */
    val revertedBy: String? = null,
    @Column(name = "reverted_at")
    /** The timestamp of when the punishment was reverted, if applicable */
    val revertedAt: Long? = null,
    @Column(name = "reverted_reason")
    /** The reason for the reversion, if applicable */
    val revertedReason: String? = null,
): Bindings {
    companion object {
        val tableDefinition = """
            CREATE TABLE IF NOT EXISTS punishments (
                id TEXT PRIMARY KEY,
                moderator TEXT NOT NULL,
                player TEXT NOT NULL,
                reason TEXT NOT NULL,
                type INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                active INTEGER NOT NULL,
                notes TEXT,
                reverted_by TEXT,
                reverted_at INTEGER,
                reverted_reason TEXT
            )
        """.trimIndent()
    }

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

    /**
     *  Handles the punishment using the data provided.
     *
     *  @return The punishment object
     */
    suspend fun handle(): Punishment {
        var duration = convertDate(this.duration)
        if(this.type == PunishmentTypes.KICK) duration = 0

        Punishments.create(
            moderator = this.moderator.uuid,
            player = this.player.uuid,
            reason = this.reason,
            type = this.type.ordinal,
            duration = duration,
            notes = this.notes,
            active = true,
        )

        val logPlaceholders = this.getPlaceholders()
            .plus(mapOf("id" to "punishment.id"))

        val key = if(this.type == PunishmentTypes.KICK) "punishments.kickedPlayer" else "punishments.punishment"
        val logString = messageUtil.getString(key).replacePlaceholders(logPlaceholders).trimIndent()

        Bukkit.getOnlinePlayers().filter { it.hasPermission("maple.staff.punish") }
            .forEach {
                it.sendMessage(logString.mm())
            }

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