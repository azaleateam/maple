@file:Listener

package team.azalea.maple.punishments.listener

import kotlinx.coroutines.runBlocking
import me.honkling.commando.common.annotations.Listener
import org.bukkit.Bukkit
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import team.azalea.maple.punishments.Punishment
import team.azalea.maple.punishments.PunishmentTypes
import team.azalea.maple.punishments.Punishments
import team.azalea.maple.punishments.User
import java.util.UUID

@Suppress("unused")
fun preLogin(event: AsyncPlayerPreLoginEvent ) = runBlocking {
    val punishments = Punishments.list(event.uniqueId)

    val activeBan = punishments.find {
        it.active == 1 && PunishmentTypes.isBan(it.type)
    }

    if(activeBan !== null) {
        val moderatorUUID = UUID.fromString(activeBan.moderator)
        val moderatorUsername = Bukkit.getOfflinePlayer(moderatorUUID).name ?: "Console"

        val punishment = Punishment(
            moderator = User(moderatorUUID, moderatorUsername),
            player = User(event.uniqueId, event.name),
            reason = activeBan.reason,
            type = PunishmentTypes.BAN,
        )

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishment.getDisconnectMessage())
    }
}