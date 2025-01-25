@file:Command("rules")

package team.azalea.maple.commands

import me.honkling.commando.common.annotations.Command
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import team.azalea.maple.messageUtil
import team.azalea.maple.util.fixString
import team.azalea.maple.util.mm
import team.azalea.maple.util.replacePlaceholders

fun rules(player: Player) {
    val pages = messageUtil.getList("rules.all").map {
        it.trimIndent().fixString().replacePlaceholders(emptyMap()).mm()
    }

    val bookTitle = Component.empty()
    val book = Book.book(bookTitle, "Azalea Team".mm(), pages)
    player.openBook(book)
}