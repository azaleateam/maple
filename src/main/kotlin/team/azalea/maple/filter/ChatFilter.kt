package team.azalea.maple.filter

/*
    @author AppleFlavored, recal
*/

import cc.ekblad.toml.decode
import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.tomlMapper
import org.bukkit.entity.Player
import team.azalea.maple.filter.ruleset.RegexWordList
import team.azalea.maple.filter.ruleset.TextWordList
import team.azalea.maple.filter.ruleset.WordList
import org.slf4j.LoggerFactory
import team.azalea.maple.filter.parsing.Tokenizer
import team.azalea.maple.maplePlugin
import team.azalea.maple.util.mm
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.notExists

val chatFilterInstance = ChatFilter()

class ChatFilter {
    private val logger = LoggerFactory.getLogger(ChatFilter::class.java)

    private var config: FilterConfig
    val configFile = maplePlugin.dataFolder.resolve("filter.toml")
    private val mapper = tomlMapper {
        mapping<FilterConfig>(
            "ruleset" to "rulesets"
        )
        decoder { tomlString: TomlValue.String -> FilterAction.valueOf(tomlString.value.uppercase()) }
    }

    init {
        maplePlugin.dataFolder.mkdirs()

        if (!configFile.exists()) {
            maplePlugin.saveResource(configFile.name, false)
        }

        config = mapper.decode(configFile.readText())
    }

    private val tokenizer = Tokenizer()
    private val wordLists = ArrayList<WordList>()

    val logChannel get() = config.root.logChannel

    init {
        config = loadConfiguration()
        if (config.rulesets == null) {
            logger.warn("The chat filter disabled! No rulesets were defined in the configuration file.")
        }

        loadRulesets()
    }

    fun validateMessage(player: Player, message: String): FilterResult {
        val tokens = tokenizer.tokenize(message)
        for (wordList in wordLists) {
            tokens.firstOrNull {wordList.test(it) }?.let { failedToken ->
                player.sendMessage(config.root.message.mm())
                return FilterResult(wordList.ruleset, listOf(failedToken))
            }
        }
        return FilterResult(null, emptyList())
    }

    fun reloadFilterConfiguration() {
        val newConfiguration = loadConfiguration()
        if (newConfiguration.rulesets == null) {
            logger.warn("No filter rulesets were defined. Using previous word list instead.")
            return
        }

        config = newConfiguration
        loadRulesets()
    }

    private fun loadConfiguration(): FilterConfig {
        logger.info("Loading filter configuration...")
        val maybeConfig = runCatching { mapper.decode<FilterConfig>(configFile.readText()) }
        return maybeConfig.getOrElse {
            logger.error("Failed to load filter configuration. Loading default instead.", maybeConfig.exceptionOrNull())
            val stream = javaClass.getResourceAsStream("/filter.toml")
                ?: throw IllegalStateException("Resource 'filter.toml' is missing in the JAR file.")
            return mapper.decode<FilterConfig>(stream)
        }
    }

    private fun loadRulesets() {
        wordLists.clear()
        for (set in config.rulesets.orEmpty()) {
            val path = Path(maplePlugin.dataFolder.path, set.path)
            if (path.notExists()) {
                logger.warn("Filter ruleset specifies path '${path.absolute()}' that does not exist. Skipping...")
                continue
            }

            val wordList = if (set.regex) {
                RegexWordList(set, path)
            } else {
                TextWordList(set, path)
            }
            wordLists.add(wordList)
        }
        wordLists.sortByDescending { it.ruleset.priority }
        logger.info("Loaded ${wordLists.size} filter rulesets.")
    }
}