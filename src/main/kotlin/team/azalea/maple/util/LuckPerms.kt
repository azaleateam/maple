package team.azalea.maple.util

import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player

val luckPerms = LuckPermsProvider.get()
val playerAdapter = luckPerms.getPlayerAdapter(Player::class.java)

