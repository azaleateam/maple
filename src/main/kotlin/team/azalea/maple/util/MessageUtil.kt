package team.azalea.maple.util

import com.moandjiezana.toml.Toml
import net.kyori.adventure.text.Component
import team.azalea.maple.maplePlugin

class MessageUtil private constructor(private val translations: Toml) {
    /**
     * Translates a key in the translations file.
     *
     * @param key The key to translate
     * @param placeholders A map of placeholders to replace in the translation
     * @return The translated string
     */
    fun translate(key: String, placeholders: Map<String, Any> = emptyMap()): String {
        val translation = translations.getString("en.$key") ?: return key
        val formatted = translation.replacePlaceholders(placeholders)
        return formatted
    }

    /**
     * Gets the lore for a key in the translations file.
     *
     * @param key The key to get the lore for
     * @return A list of components representing the lore
     */
    fun getLore(key: String): List<Component> {
        val translation = translations.getString("en.$key") ?: return emptyList()
        return translation.split("\n").map {
            if(it.isEmpty()) {
                Component.empty()
            } else {
                it.trimIndent().mm()
            }
        }
    }

    /**
     * Gets the string for a key in the translations file.
     *
     * @param key The key to get the string for
     * @return The string
     */
    fun getString(key: String): String = translations.getString("en.$key") ?: key

    fun getList(key: String): List<String> = translations.getList("en.$key")

    /**
     * Gets the colors inside the translations file.
     *
     * @return A map of colors
     */
    fun getColors(): Map<String, Any> = translations.getTable("colors").toMap()

    /**
     * Gets the custom placeholders inside the translations file.
     *
     * @return A map of custom placeholders
     */
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
