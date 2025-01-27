package team.azalea.maple.util

private val cooldowns: MutableMap<String, Cooldown> = mutableMapOf()

class Cooldown() {
    var key = ""
    var duration = 0L
    private var startTime = 0L

    private fun reset() {
        duration = 0L
        startTime = 0L
    }

    fun isActive(): Boolean {
        if (startTime == 0L) return false
        val currentTime = System.currentTimeMillis()
        return (currentTime - startTime) < duration
    }

    fun getRemainingTime(): Long {
        if (startTime == 0L) return 0L
        val currentTime = System.currentTimeMillis()
        return (duration - (currentTime - startTime)).coerceAtLeast(0L)
    }

    fun startCooldown(): Cooldown {
        this.startTime = System.currentTimeMillis()
        return this
    }
}

fun handleCooldown(init: Cooldown.() -> Unit): Cooldown {
    val cooldown = Cooldown()
    cooldown.init()
    return cooldowns.getOrPut(cooldown.key) {
        cooldown
    }
}

fun debounce(time: Long, identifier: String, block: () -> Unit) {
    val cooldown = handleCooldown {
        key = "debounce-${identifier}"
        duration = time
    }

    if(cooldown.isActive()) return

    cooldown.startCooldown()
    block()
}