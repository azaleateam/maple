package team.azalea.maple.punishments

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.ingot.iron.Iron
import gg.ingot.iron.bindings.bind
import gg.ingot.iron.strategies.NamingStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            notes = notes,
            active = if(active) 1 else 0,
        )

        iron.prepare("""
            INSERT INTO punishments (id, moderator, player, reason, type, created_at, updated_at, duration, active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
        )

        return punishment
    }

    suspend fun list(player: UUID): List<PunishmentData> {
        return iron.prepare("""
            SELECT * FROM punishments
        """.trimIndent()
        ).all<PunishmentData>()
    }

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
}