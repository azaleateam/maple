package team.azalea.maple.discord

import net.dv8tion.jda.api.JDA

fun useBot(block: (client: JDA) -> Unit) {
    try {
        if (!Discord.isConnected()) return
        block(bot)
    } catch (
        // this exception is thrown when the bot token is empty
        e: UninitializedPropertyAccessException
    ) {
        return
    }
}
