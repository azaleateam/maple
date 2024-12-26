package team.azalea.maple.models

import gg.ingot.iron.annotations.Model
import gg.ingot.iron.bindings.Bindings
import gg.ingot.iron.strategies.NamingStrategy

@Model(
    naming = NamingStrategy.SNAKE_CASE,
    table = "server_settings",
)
data class ServerSettings(
    val key: String,
    val value: String,
): Bindings