package team.azalea.maple.listener

import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import team.azalea.maple.punishments.PunishmentTypes
import team.azalea.maple.punishments.Punishments
import team.azalea.maple.punishments.getReasonInfo
import team.azalea.maple.util.sendKey

class PlayerChatListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event: AsyncChatEvent) = runBlocking {
        val player = event.player

        val activeMute = Punishments.list(player.uniqueId).find {
            it.active == 1 && it.type == PunishmentTypes.MUTE.ordinal
        }

        if(activeMute !== null) {
            event.isCancelled = true

            val reasonInfo = getReasonInfo(activeMute.reason)

            val placeholders = mapOf(
                "short_reason" to reasonInfo.first,
                "full_reason" to reasonInfo.second,
                "duration" to activeMute.getClass().getFormattedDuration(),
            )

            player.sendKey(
                "punishments.commands.mute.playerMuted",
                placeholders
            )
        }
    }
}

