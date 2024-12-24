package team.azalea.maple.util

import com.moandjiezana.toml.Toml
import team.azalea.maple.maplePlugin

class MessageUtil private constructor(private val translations: Toml) {
    fun translate(key: String, placeholders: Map<String, String> = emptyMap()): String {
        val translation = translations.getString("en.$key") ?: return key
        val formatted = translation.replacePlaceholders(placeholders)
        return formatted
    }

    fun getString(key: String): String = translations.getString("en.$key") ?: key

    fun getColors(): Map<String, Any> = translations.getTable("colors").toMap()

    fun getCustomPlaceholders(): Map<String, Any> = translations.getTable("custom_placeholders").toMap()

    class Builder {
        private lateinit var translations: Toml

        fun load(): Builder {
            maplePlugin.dataFolder.mkdirs()
            val configFile = maplePlugin.dataFolder.resolve("translations.toml")

            if (!configFile.exists()) {
                maplePlugin.saveResource(configFile.name, false)
            }

            translations = Toml().read(configFile.readText())
            return this
        }

        fun build(): MessageUtil {
            if (!::translations.isInitialized) {
                throw IllegalStateException("Translations must be loaded before building MessageUtil")
            }
            return MessageUtil(translations)
        }
    }

    companion object {
        private lateinit var instance: MessageUtil

        fun initialize(): MessageUtil {
            val builder = Builder().load()
            instance = builder.build()
            return instance
        }

        fun getInstance(): MessageUtil {
            if (!::instance.isInitialized) {
                throw IllegalStateException("MessageUtil is not initialized. Call initialize() first.")
            }
            return instance
        }
    }
}
