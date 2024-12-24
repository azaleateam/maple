package team.azalea.maple

import com.moandjiezana.toml.Toml
import java.nio.file.Files

object Translations {
    private lateinit var translations: Toml

    init {
        loadTranslations()
    }

    private fun loadTranslations() {
        instance.dataFolder.mkdirs()
        val configFile = instance.dataFolder.resolve("translations.toml")

        if (!configFile.exists()) {
            println("Translations file not found, creating... ${configFile.name}")
            instance.saveResource(configFile.name, false)
        }

        val data = String(Files.readAllBytes(configFile.toPath()))
        translations = Toml().read(data)
    }

    fun translate(key: String, vararg args: Any): String {
        val translation = translations.getString("en.$key") ?: return key
        val formatted = String.format(translation, *args)
        return formatted.trimIndent()
    }

    fun getString(key: String): String {
        return translations.getString("en.$key") ?: key
    }

    fun reload() {
        loadTranslations()
    }
}