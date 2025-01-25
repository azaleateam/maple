package team.azalea.maple

import gg.ingot.iron.Iron
import org.flywaydb.core.Flyway

object Database {
    private val dataFolder = maplePlugin.dataFolder.resolve("data")

    private val iron = Iron("jdbc:sqlite:${dataFolder.absolutePath}/maple.db")

    init {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        iron.connect()
    }

    /**
     * Applies all migrations to the database
     */
    fun migrate() {
        Flyway.configure(maplePlugin.javaClass.classLoader)
            .dataSource(iron.pool)
            .locations("classpath:db/migration")
            .sqlMigrationPrefix("")
            .sqlMigrationSeparator("_")
            .outOfOrder(true)
            .load()
            .migrate()
    }

    suspend fun getServerName(): String {
        val iron = getIron()
        val serverName = iron.prepare("SELECT value FROM server_settings WHERE key = 'server_name'")
            .singleNullable<String>()
        return serverName ?: "Unknown Server"
    }

    suspend fun getMinehutInfo(): Pair<String, String> {
        val iron = getIron()
        val minehutName = iron.prepare("SELECT value FROM server_settings WHERE key = 'minehut_name'")
            .singleNullable<String>()
        val minehutId = iron.prepare("SELECT value FROM server_settings WHERE key = 'minehut_id'")
            .singleNullable<String>()
        return Pair(minehutName ?: "Unknown Name", minehutId ?: "Unknown ID")
    }

    fun getIron(): Iron = iron
}