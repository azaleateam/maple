package team.azalea.maple.filter.ruleset

/*
    @author AppleFlavored, recal
*/

import team.azalea.maple.filter.FilterConfig
import team.azalea.maple.filter.parsing.Tag
import team.azalea.maple.filter.parsing.TokenSpan
import java.nio.file.Files
import java.nio.file.Path

class RegexWordList(override val ruleset: FilterConfig.Ruleset, path: Path) : WordList {
    private val patterns: List<Regex> = Files.readAllLines(path).map { Regex(it, RegexOption.IGNORE_CASE) }

    override fun test(token: TokenSpan): Boolean {
        if (token.tag != Tag.WORD && token.tag != Tag.MIXED_WORD) {
            return false
        }
        return patterns.any { it.matches(token.value) }
    }
}