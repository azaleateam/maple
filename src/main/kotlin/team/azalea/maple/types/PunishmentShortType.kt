package team.azalea.maple.types

import me.honkling.commando.common.generic.ICommandSender
import me.honkling.commando.common.types.Type
import team.azalea.maple.punishments.punishmentConfig

class PunishmentShort(private val value: String) {
    override fun toString(): String = value
}

object PunishmentType : Type<PunishmentShort>() {
    private val punishments = punishmentConfig.punishments.keys

    override fun validate(sender: ICommandSender<*>, input: String): Boolean {
        val first = input.split(" ").first().lowercase()
        return punishments.contains(first.lowercase())
    }

    override fun parse(sender: ICommandSender<*>, input: String): Pair<PunishmentShort, Int> {
        val first = input.split(" ").first().lowercase()
        val punishment = punishments.find { it.equals(first, ignoreCase = true) }
            ?: throw IllegalArgumentException("Invalid punishment: $input")
        return PunishmentShort(punishment) to 1
    }

    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        if (input.isEmpty()) return punishments.toList()
        return punishments.filter { it.contains(input, ignoreCase = true) && it != input }
    }

}