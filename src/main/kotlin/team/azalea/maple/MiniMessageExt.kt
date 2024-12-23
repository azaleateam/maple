package team.azalea.maple

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

fun String.mm(): Component {
    return MiniMessage.miniMessage().deserialize(this).decoration(TextDecoration.ITALIC, false)
}

fun Component.plainText(): String {
    return PlainTextComponentSerializer.plainText().serialize(this)
}

/**
   Converts a message to a string with legacy formatting.

   This is used for places that don't support components natively, like GUI titles.
*/
fun String.toLegacy(section: Boolean = true) = mm().toLegacy(section)

/**
   Converts a component to a string with legacy formatting.

   This is used for places that don't support components natively, like GUI titles.
 */
fun Component.toLegacy(section: Boolean = true): String {
    val serializer =
        if (section) LegacyComponentSerializer.legacySection()
        else LegacyComponentSerializer.legacyAmpersand()
    return serializer.serialize(this)
}