package team.azalea.maple.util

import org.bukkit.entity.Player
import team.azalea.maple.messageUtil

fun Player.sendKey(key: String, placeholders: Map<String, String> = emptyMap()) {
    this.sendMessage(messageUtil.translate(key, placeholders).trimIndent().mm())
}

/*
    convenience method for sending a key with a single placeholder

    example: player.sendKey("hello.world", "x" to y)
    rather than: player.sendKey("hello.world", mapOf("x" to y))
*/
fun Player.sendKey(key: String, placeholders: Pair<String, String>) {
    this.sendKey(key, mapOf(placeholders.first to placeholders.second))
}