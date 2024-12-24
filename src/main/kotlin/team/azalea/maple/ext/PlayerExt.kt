package team.azalea.maple.ext

import org.bukkit.entity.Player
import team.azalea.maple.Translations

fun Player.sendKey(key: String, vararg args: Any) {
    this.sendMessage(Translations.translate(key, *args).mm())
}