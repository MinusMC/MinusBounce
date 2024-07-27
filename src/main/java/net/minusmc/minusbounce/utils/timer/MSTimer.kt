/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.timer

class MSTimer {
    var time = -1L

    fun hasTimePassed(delay: Int) = hasTimePassed(delay.toLong())

    fun hasTimePassed(delay: Long): Boolean {
        return System.currentTimeMillis() >= time + delay
    }

    fun hasTimeLeft(delay: Long): Long {
        return delay + time - System.currentTimeMillis()
    }

    val reachedTime: Long
        get() = System.currentTimeMillis() - time

    fun reset() {
        time = System.currentTimeMillis()
    }
}
