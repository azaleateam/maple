package team.azalea.maple.punishments

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.ingot.iron.Iron
import gg.ingot.iron.bindings.bind
import org.bukkit.Bukkit
import org.flywaydb.core.Flyway
import team.azalea.maple.commandManager
import team.azalea.maple.listenerManager
import team.azalea.maple.maplePlugin
import team.azalea.maple.types.PunishmentShort
import team.azalea.maple.types.PunishmentType
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

    private val iron = Iron("jdbc:sqlite:${dataFolder.absolutePath}/punishments.db")

    init {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        iron.connect()
    }

    /**
     * Applies all migrations to the database
     */
    private fun migrate() {
        Flyway.configure(maplePlugin.javaClass.classLoader)
            .dataSource(iron.pool)
            .locations("classpath:db/migration")
            .sqlMigrationPrefix("")
            .sqlMigrationSeparator("_")
            .outOfOrder(true)
            .load()
            .migrate()
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
     *  Reverts a punishment.
     *
     *  @param id The ID of the punishment
     *  @param user The user who is reverting the punishment
     *  @param reason The reason for the revert
     */
    suspend fun revert(id: String, user: User, reason: String) {
        val punishment = get(id) ?: throw IllegalArgumentException("Punishment with ID $id does not exist.")
        if(punishment.type == PunishmentTypes.KICK.ordinal) throw IllegalStateException("Kicks cannot be reverted.")
        if(punishment.active == 0) throw IllegalStateException("Punishment with ID $id is not active.")
        if(punishment.revertedAt != null) throw IllegalStateException("Punishment with ID $id has already been reverted.")

        iron.prepare("""
            UPDATE punishments
            SET 
                reverted_by = :user, 
                reverted_at = :time, 
                reverted_reason = :reason,
                active = 0
            WHERE id = :id
        """.trimIndent(), bind {
            "user" to user.uuid
            "time" to System.currentTimeMillis()
            "reason" to reason
            "id" to id
        })
    }

    /**
     *  Updates the reason of a punishment.
     *
     *  @param id The ID of the punishment
     *  @param reason The new reason for the punishment
     */
    suspend fun updateReason(id: String, reason: String) {
        iron.prepare("""
            UPDATE punishments
            SET reason = :reason
            WHERE id = :id
        """.trimIndent(), bind {
            "reason" to reason
            "id" to id
        })
    }

    /**
     *  Updates the notes of a punishment.
     *
     *  @param id The ID of the punishment
     *  @param notes The new notes for the punishment
     */
    suspend fun updateNotes(id: String, notes: String?) {
        // this is seperate because Iron doesn't support null values in binds
        if(notes === null) {
            iron.prepare("""
                UPDATE punishments
                SET notes = NULL
                WHERE id = ?
            """.trimIndent(), id)
            return
        }

        iron.prepare("""
            UPDATE punishments
            SET notes = :notes
            WHERE id = :id
        """.trimIndent(), bind {
            "notes" to notes
            "id" to id
        })
    }

    /**
     * Updates the duration of a punishment.
     *
     * @param id The ID of the punishment
     * @param duration The new duration of the punishment
     */
    suspend fun updateDuration(id: String, duration: Long) {
        val punishment = get(id) ?: throw IllegalArgumentException("Punishment with ID $id does not exist.")
        if(punishment.type == PunishmentTypes.KICK.ordinal) throw IllegalStateException("Kicks cannot have a duration.")

        iron.prepare("""
            UPDATE punishments
            SET duration = :duration
            WHERE id = :id
        """.trimIndent(), bind {
            "duration" to duration
            "id" to id
        })
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

        migrate()

        commandManager.types[PunishmentShort::class.java] = PunishmentType

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