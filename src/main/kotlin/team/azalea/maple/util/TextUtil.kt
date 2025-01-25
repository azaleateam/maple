package team.azalea.maple.util

import team.azalea.maple.messageUtil

/**
 * Replaces placeholders in a string with the provided map.
 *
 * The placeholders are of the format {placeholder} or {placeholder_modifier}.
 * The modifier is optional and can be one of the following:
 * - lowercase: Converts the placeholder to lowercase
 * - uppercase: Converts the placeholder to uppercase
 * - capitalized: Capitalizes the first letter of the placeholder
 *
 * @param map The map of placeholders to replace
 * @param ignoreCase Whether to ignore the case of the placeholder
 * @return The string with placeholders replaced
 */
fun String.replacePlaceholders(map: Map<String, Any>, ignoreCase: Boolean = false): String {
    val modifiers: Map<String, (String) -> String> = mapOf(
        "lowercase" to String::lowercase,
        "uppercase" to String::uppercase,
        "capitalized" to String::capitalize,
    )

    val totalPlaceholders = mutableMapOf<String, String>()
    val customPlaceholders = messageUtil.getCustomPlaceholders()

    customPlaceholders.forEach {
        totalPlaceholders[it.key] = it.value.toString()
    }
    totalPlaceholders.putAll(map.map { it.key to it.value.toString() })

    var placeholded = this

    totalPlaceholders.forEach { (key, value) ->
        val regex = """\{($key(?:_[a-zA-Z]+)?)\}""".toRegex()
        placeholded = placeholded.replace(regex) { matchResult ->
            val placeholder = matchResult.groupValues[1]

            val parts = placeholder.split("_")
            val baseKey = parts[0]  // original placeholder (ex., "reason")
            val modifier = parts.getOrNull(1)  // modifier (ex., "capitalized")

            val baseValue = totalPlaceholders[baseKey] ?: value

            // runs the transformation function for the modifier, if there is one
            // otherwise, just return the base value
            modifier?.let {
                val transformation = modifiers[it]
                // if there's a valid transformation for the modifier, apply it
                if (transformation != null) transformation(baseValue)
                else baseValue
            } ?: baseValue
        }
    }

    return placeholded
}

/**
 * Replaces tabs with empty strings, these tab operators can typically break books
 */
fun String.fixString(): String = this.replace("\t", "").replace("\r", "")