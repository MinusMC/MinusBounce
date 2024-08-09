/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object AnimationUtils {
    fun easeOut(t: Float, d: Float) = easeOut(t.toDouble(), d.toDouble()).toFloat()

    fun easeOut(t: Double, d: Double) = EaseUtils.easeInCubic(t / d - 1) + 1

    fun animate(target: Float, current: Float, pSpeed: Float) = animate(target.toDouble(), current.toDouble(), pSpeed.toDouble()).toFloat()

    fun animate(target: Double, current: Double, pSpeed: Double): Double {
        if (current == target)
            return current

        var speed = pSpeed.coerceIn(0.0, 1.0)
        
        val diff = max(target, current) - min(target, current)
        var factor = (diff * pSpeed).coerceAtLeast(0.1)

        return if (target > current) min(current + factor, target)
            else if (target < current) max(current - factor, target)
            else current
    }

    fun changer(current: Double, add: Double, min: Double, max: Double) = (current + add).coerceIn(min, max)

    fun changer(current: Float, add: Float, min: Float, max: Float) = (current + add).coerceIn(min, max)
}
