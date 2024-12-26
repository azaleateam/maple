package team.azalea.maple.filter

/*
    @author AppleFlavored, recal
*/

data class FilterConfig(
    val root: Root,
    val rulesets: List<Ruleset>?
) {
    data class Root(
        val message: String = "Uh oh! Your message was blocked.",
        val logChannel: String?
    )

    data class Ruleset(
        val priority: Int = 1,
        val action: FilterAction = FilterAction.BLOCK,
        val banReason: String = "hate",
        val path: String,
        val regex: Boolean = false
    )
}
