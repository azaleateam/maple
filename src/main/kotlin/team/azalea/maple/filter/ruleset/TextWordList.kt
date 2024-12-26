package team.azalea.maple.filter.ruleset

/*
    @author AppleFlavored, recal
*/

import team.azalea.maple.filter.FilterConfig
import team.azalea.maple.filter.parsing.Tag
import team.azalea.maple.filter.parsing.TokenSpan
import java.nio.file.Files
import java.nio.file.Path

class TextWordList(override val ruleset: FilterConfig.Ruleset, path: Path) : WordList {
    private val words: List<String> = Files.readAllLines(path)

    override fun test(token: TokenSpan): Boolean {
        if (token.tag != Tag.WORD) {
            return false
        }
        return words.contains(token.value)
    }
}