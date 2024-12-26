package team.azalea.maple.filter.parsing

/*
    @author AppleFlavored, recal
*/

data class TokenSpan(val value: String, val tag: Tag, val start: Int)

enum class Tag {
    WORD,
    MIXED_WORD,
    NUMBER,
    PUNCTUATION,
    UNKNOWN,
}