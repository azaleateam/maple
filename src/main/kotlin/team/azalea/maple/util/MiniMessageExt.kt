package team.azalea.maple.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import team.azalea.maple.messageUtil
import team.azalea.maple.miniMessage

class MapleMiniMessage {
    fun build(): MiniMessage {
        val format = mutableListOf<TagResolver>()

        val colors = messageUtil.getColors()

        fun addDefaultColorCode(code: String, color: String) {
            if (colors.contains(code)) return
            format.add(createBasicColorResolver(code, color))
        }

        addDefaultColorCode("p", "#FBB13C")
        addDefaultColorCode("s", "#EFEFEF")
        addDefaultColorCode("t", "#FF5700")

        colors.keys.forEach {
            format.add(createBasicColorResolver(it, colors[it].toString()))
        }

        val resolvers = TagResolver.resolver(
            StandardTags.defaults(),
            *format.toTypedArray()
        )

        val builder = MiniMessage.builder()
            .tags(resolvers)

        return builder.build()
    }

    private fun createBasicColorResolver(name: String, color: String) =
        TagResolver.resolver(name, Tag.styling(TextColor.fromHexString(color)!!))
}

fun String.mm(
    vararg resolvers: TagResolver,
): Component {
    return miniMessage.deserialize(this, *resolvers)
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
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