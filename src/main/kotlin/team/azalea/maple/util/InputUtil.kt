package team.azalea.maple.util

import io.papermc.paper.event.player.AsyncChatEvent
import me.honkling.commonlib.lib.scheduleTemporarily
import org.bukkit.entity.Player
import java.util.EventListener

typealias PromptHandler = (String) -> Unit

fun isConfirmed (input: String): Boolean = input.equals("yes", true) || input.equals("y", true) || input.equals("confirm", true)

object InputHandler {
    internal val usingUnfilteredInput = mutableMapOf<Player, Boolean>()
    fun isUsingUnfiltered(player: Player) = usingUnfilteredInput[player] ?: false
}

class Input {
    /**
     * The Player you want to fetch the input from.
     */
    var player: Player? = null
    /**
     * The handler for the input.
     */
    var handler: PromptHandler = {}

    /**
     * Whether to bypass the chat filter.
     * */
    var bypassFilter = false

    /**
     * Waits for the player to input a message, and then calls the handler with the input.
     */
    fun prompt(): Input {
        if(player == null) throw IllegalStateException("Player is null")

        if(bypassFilter) {
            InputHandler.usingUnfilteredInput[player!!] = true
        }

        scheduleTemporarily {
            subscribe(AsyncChatEvent::class) { event ->
                val message = event.message().plainText()

                if (event.player != player) return@subscribe

                event.isCancelled = true
                if (message.equals("cancel", ignoreCase = true)) {
                    event.player.sendMessage("<s>Input cancelled.".mm())
                }
                else handler(message)

                resolve()
            }
        }

        return this
    }
}

/**
 * Allows you to fetch an input from a player.
 *
 * @param player The player to fetch the input from.
 * @param handler The handler for the input.
 *
 * @return An [Input] object.
 */
fun fetchInput(player: Player, handler: PromptHandler) =  fetchInput(player, false, handler)

/**
 * Allows you to fetch an input from a player.
 *
 * @param player The player to fetch the input from.
 * @param bypassFilter If true, the input will not be processed by the chat filter.
 * @param handler The handler for the input.
 *
 * @return An [Input] object.
 */
fun fetchInput(player: Player, bypassFilter: Boolean = false, handler: PromptHandler): Input {
    val input = Input()
    input.handler = handler
    input.player = player
    input.bypassFilter = bypassFilter
    return input
}
