package team.azalea.maple.filter

/*
    @author AppleFlavored, recal
*/

import team.azalea.maple.filter.parsing.TokenSpan

data class FilterResult(val ruleset: FilterConfig.Ruleset?, val failedTokens: List<TokenSpan>)