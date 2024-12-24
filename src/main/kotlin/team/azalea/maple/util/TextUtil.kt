package team.azalea.maple.util

import team.azalea.maple.messageUtil

fun String.replacePlaceholders(map: Map<String, String>, ignoreCase: Boolean = false) : String {
    val parenthesis = "{}"

    val totalPlaceholders = mutableMapOf<String, String>()
    val customPlaceholders = messageUtil.getCustomPlaceholders()

    customPlaceholders.forEach {
        totalPlaceholders[it.key] = it.value.toString()
    }
    totalPlaceholders.putAll(map)

    var placeholded = this
    for (value in totalPlaceholders) {
        placeholded = placeholded.replace("${parenthesis[0]}${value.key}${parenthesis[1]}", value.value, ignoreCase)
    }
    return placeholded
}