package net.minusmc.minusbounce.utils.timer

class TimerUtil {
    var lastMS = System.currentTimeMillis()
    fun reset() {
        lastMS = System.currentTimeMillis()
    }

    fun hasTimeElapsed(time: Long, reset: Boolean): Boolean {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset) reset()
            return true
        }
        return false
    }

    fun hasTimeElapsed(time: Long): Boolean {
        return System.currentTimeMillis() - lastMS > time
    }

    fun hasTimeElapsed(time: Double): Boolean {
        return hasTimeElapsed(time.toLong())
    }

    var time: Long
        get() = System.currentTimeMillis() - lastMS
        set(time) {
            lastMS = time
        }
}