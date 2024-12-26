package team.azalea.maple.filter.ruleset

/*
    @author AppleFlavored, recal
*/

import team.azalea.maple.filter.FilterConfig
import team.azalea.maple.filter.parsing.TokenSpan

interface WordList {
    val ruleset: FilterConfig.Ruleset
    fun test(token: TokenSpan): Boolean
}