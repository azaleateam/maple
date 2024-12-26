package team.azalea.maple.util

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import team.azalea.maple.messageUtil
import java.util.UUID


fun CommandSender.sendKey(key: String, placeholders: Map<String, String> = emptyMap()) {
    this.sendMessage(messageUtil.translate(key, placeholders).trimIndent().mm())
}

/*
    convenience method for sending a key with a single placeholder

    example: player.sendKey("hello.world", "x" to y)
    rather than: player.sendKey("hello.world", mapOf("x" to y))
*/
fun CommandSender.sendKey(key: String, placeholders: Pair<String, String>) {
    this.sendKey(key, mapOf(placeholders.first to placeholders.second))
}


/**
    Check if a player can be actioned by another player

    * @param targetUUID the UUID of the player the moderator is trying to execute the action on
    * @param moderatorUUID the UUID of the player that is trying to execute the action
*/
private fun canPlayerBeActionedBy(targetUUID: UUID, moderatorUUID: UUID): Boolean {
    val targetPlayer = Bukkit.getOfflinePlayer(targetUUID)

    if(!targetPlayer.hasPlayedBefore()) return true

    val targetUser = luckPerms.userManager.getUser(targetUUID) ?: return true
    val moderatorUser = luckPerms.userManager.getUser(moderatorUUID)
        ?: throw IllegalArgumentException("Failed to fetch LuckPerms user for $moderatorUUID")

    val targetGroup = targetUser.primaryGroup
    val moderatorGroup = moderatorUser.primaryGroup

    val targetGroupWeight = luckPerms.groupManager.getGroup(targetGroup)?.weight?.orElse(0) ?: 0
    val moderatorGroupWeight = luckPerms.groupManager.getGroup(moderatorGroup)?.weight?.orElse(0) ?: 0

    val isTargetOp = Bukkit.getPlayer(targetUUID)?.isOp ?: false

    return targetGroupWeight < moderatorGroupWeight && !isTargetOp
}

/**
    check if a player can be actioned by another player

    * @param moderator the player that is trying to execute the action
*/
fun Player.canBeActionedBy(moderator: Player): Boolean {
    return canPlayerBeActionedBy(this.uniqueId, moderator.uniqueId)
}

/**
    check if an offline player can be actioned by another player

    * @param moderator the player that is trying to execute the action
*/
fun OfflinePlayer.canBeActionedBy(moderator: Player): Boolean {
    return canPlayerBeActionedBy(this.uniqueId, moderator.uniqueId)
}