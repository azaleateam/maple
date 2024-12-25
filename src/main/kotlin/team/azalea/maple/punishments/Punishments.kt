package team.azalea.maple.punishments

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.ingot.iron.Iron
import gg.ingot.iron.strategies.NamingStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import team.azalea.maple.commandManager
import team.azalea.maple.listenerManager
import team.azalea.maple.maplePlugin
import java.util.UUID

data class PunishmentConfig(
    val shortReason: String,
    val longReason: String = shortReason,
    val action: String,
    val duration: List<String>? = null,
)

data class PunishmentsConfig(
    val punishments: Map<String, PunishmentConfig>
)

lateinit var punishmentConfig: PunishmentsConfig

object Punishments {
    private val mapper = tomlMapper {}

    private val dataFolder = maplePlugin.dataFolder.resolve("data")

    private val iron = Iron("jdbc:sqlite:${dataFolder.absolutePath}/pixel_data.db") {
        namingStrategy = NamingStrategy.SNAKE_CASE
    }

    init {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        iron.connect()

        CoroutineScope(Dispatchers.IO).launch {
            iron.execute(PunishmentData.tableDefinition)
        }
    }

    /**
     *  Creates a new punishment.
     *
     *  @param moderator The UUID of the moderator
     *  @param player The UUID of the player
     *  @param reason The reason for the punishment
     *  @param type The type of punishment
     *  @param duration The duration of the punishment
     *  @param notes The notes attached to the punishment
     *  @param active Whether the punishment is active
     *  @return A PunishmentData object representing the created punishment
     */
    suspend fun create(
        moderator: UUID,
        player: UUID,
        reason: String,
        type: Int,
        duration: Long,
        notes: String? = null,
        active: Boolean = true
    ): PunishmentData {
        val punishment = PunishmentData(
            moderator = moderator.toString(),
            player = player.toString(),
            reason = reason,
            type = type,
            duration = duration,
            active = if(active) 1 else 0,
            notes = notes,
        )

        iron.prepare("""
            INSERT INTO punishments (id, moderator, player, reason, type, created_at, updated_at, duration, active, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(),
            punishment.id,
            punishment.moderator,
            punishment.player,
            punishment.reason,
            punishment.type,
            punishment.createdAt,
            punishment.updatedAt,
            punishment.duration,
            punishment.active,
            punishment.notes,
        )

        return punishment
    }

    /**
     *  Lists all punishments for a player.
     *
     *  @param player The UUID of the player
     *  @return A list of PunishmentData objects
     */
    suspend fun list(player: UUID): List<PunishmentData> {
        return iron.prepare("""
            SELECT * FROM punishments WHERE player = ?
        """.trimIndent(), player.toString()
        ).all<PunishmentData>()
    }

    /**
     *  Gets a punishment by its ID.
     *
     *  @param id The ID of the punishment
     *  @return A PunishmentData object
     */
    suspend fun get(id: String): PunishmentData? {
        return iron.prepare("""
            SELECT * FROM punishments WHERE id = ?
        """.trimIndent(), id
        ).singleNullable<PunishmentData>()
    }

    /**
     * Sets up the punishment module by loading the config then registering commands and listeners.
     */
    fun setup() {
        maplePlugin.dataFolder.mkdirs()
        val configFile = maplePlugin.dataFolder.resolve("punishments.toml")

        if (!configFile.exists()) {
            maplePlugin.saveResource(configFile.name, false)
        }

        punishmentConfig = mapper.decode(configFile.readText())

        commandManager.registerCommands("team.azalea.maple.punishments.commands")
        listenerManager.registerListeners("team.azalea.maple.punishments.listener")
    }

    /**
     *  Gets a user from a UUID string.
     *
     *  @param uuid The UUID of the user
     *  @return A User object
     */
    fun getPlayer(uuid: String): User {
        val parsedUUID = UUID.fromString(uuid)
        if(parsedUUID == CONSOLE_USER.uuid) return CONSOLE_USER

        val username = Bukkit.getOfflinePlayer(parsedUUID).name
        return User(parsedUUID, username ?: "Unknown")
    }
}