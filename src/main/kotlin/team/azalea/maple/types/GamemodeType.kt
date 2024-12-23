package team.azalea.maple.types

import me.honkling.commando.common.generic.ICommandSender
import me.honkling.commando.common.types.Type
import org.bukkit.GameMode

object GamemodeType : Type<GameMode>() {
    override fun validate(sender: ICommandSender<*>, input: String): Boolean {
        return try {
            GameMode.valueOf(input.uppercase())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    override fun parse(sender: ICommandSender<*>, input: String): Pair<GameMode, Int> {
        return GameMode.valueOf(input.uppercase()) to 1
    }

    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        return GameMode.entries
            .map { it.name.lowercase() }
            .filter { it.startsWith(input.lowercase()) }
    }
}
