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
    fun easeOut(t: Float, d: Float): Float {
        var t = t
        return (t / d - 1).also { t = it } * t * t + 1
    }

    fun animate(target: Double, current: Double, speed: Double): Double {
        var current = current
        var speed = speed
        if (current == target) return current
        val larger = target > current
        if (speed < 0.0) {
            speed = 0.0
        } else if (speed > 1.0) {
            speed = 1.0
        }
        val dif = max(target, current) - min(target, current)
        var factor = dif * speed
        if (factor < 0.1) {
            factor = 0.1
        }
        if (larger) {
            current += factor
            if (current >= target) current = target
        } else if (target < current) {
            current -= factor
            if (current <= target) current = target
        }
        return current
    }

    fun animate(target: Float, current: Float, speed: Float): Float {
        var current = current
        var speed = speed
        if (current == target) return current
        val larger = target > current
        if (speed < 0.0f) {
            speed = 0.0f
        } else if (speed > 1.0f) {
            speed = 1.0f
        }
        val dif = max(target.toDouble(), current.toDouble()) - min(target.toDouble(), current.toDouble())
        var factor = dif * speed.toDouble()
        if (factor < 0.1) {
            factor = 0.1
        }
        if (larger) {
            current += factor.toFloat()
            if (current >= target) current = target
        } else if (target < current) {
            current -= factor.toFloat()
            if (current <= target) current = target
        }
        return current
    }

    fun changer(current: Double, add: Double, min: Double, max: Double): Double {
        var current = current
        current += add
        if (current > max) {
            current = max
        }
        if (current < min) {
            current = min
        }
        return current
    }

    fun changer(current: Float, add: Float, min: Float, max: Float): Float {
        var current = current
        current += add
        if (current > max) {
            current = max
        }
        if (current < min) {
            current = min
        }
        return current
    }

    fun easeInBack(x: Double): Double {
        val c1 = 1.70158
        val c3 = c1 + 1

        return c3 * x * x * x - c1 * x * x
    }

    fun easeOutBack(x: Double): Double {
        val c1 = 1.70158
        val c3 = c1 + 1

        return 1 + c3 * (x - 1).pow(3) + c1 * (x - 1).pow(2)
    }
}
