package net.minusmc.minusbounce.utils

import net.minecraft.client.Minecraft
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.value.BoolValue
import kotlin.math.max
import kotlin.math.min

class AnimationHelper {
    var animationX = 0f
    var alpha = 0

    constructor() {
        alpha = 0
    }

    constructor(value: BoolValue) {
        animationX = (if (value.get()) 5 else -5).toFloat()
    }

    constructor(module: Module) {
        animationX = (if (module.state) 5 else -5).toFloat()
    }

    companion object {
        fun clamp(number: Float, min: Float, max: Float): Float {
            return if (number < min) min else min(number.toDouble(), max.toDouble())
                .toFloat()
        }

        fun calculateCompensation(target: Float, current: Float, delta: Long, speed: Int): Float {
            var current = current
            var delta = delta
            val diff = current - target
            if (delta < 1L) {
                delta = 1L
            }
            val xD: Double
            if (diff > speed.toFloat()) {
                xD =
                    if ((speed.toLong() * delta / 16L).toDouble() < 0.25) 0.5 else (speed.toLong() * delta / 16L).toDouble()
                current = (current.toDouble() - xD).toFloat()
                if (current < target) {
                    current = target
                }
            } else if (diff < (-speed).toFloat()) {
                xD =
                    if ((speed.toLong() * delta / 16L).toDouble() < 0.25) 0.5 else (speed.toLong() * delta / 16L).toDouble()
                current = (current.toDouble() + xD).toFloat()
                if (current > target) {
                    current = target
                }
            } else {
                current = target
            }
            return current
        }

        fun animate(target: Double, current: Double, speed: Double): Double {
            var current = current
            var speed = speed
            val larger: Boolean = target > current
            val bl = larger
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
            current = if (larger) factor.let { current += it; current } else factor.let { current -= it; current }
            return current
        }

        fun moveUD(current: Float, end: Float, smoothSpeed: Float, minSpeed: Float): Float {
            var movement = (end - current) * smoothSpeed
            if (movement > 10.0f) {
                movement =
                    max(minSpeed.toDouble(), movement.toDouble()).toFloat()
                movement = min((end - current).toDouble(), movement.toDouble())
                    .toFloat()
            } else if (movement < 10.0f) {
                movement =
                    min((-minSpeed).toDouble(), movement.toDouble()).toFloat()
                movement = max((end - current).toDouble(), movement.toDouble())
                    .toFloat()
            }
            return current + movement
        }

        fun moveTowards(current: Float, end: Float, smoothSpeed: Float, minSpeed: Float): Float {
            var movement = (end - current) * smoothSpeed
            if (movement > 0) {
                movement = max(minSpeed.toDouble(), movement.toDouble()).toFloat()
                movement = min((end - current).toDouble(), movement.toDouble()).toFloat()
            } else if (movement < 0) {
                movement = min(-minSpeed.toDouble(), movement.toDouble()).toFloat()
                movement = max((end - current).toDouble(), movement.toDouble()).toFloat()
            }
            return current + movement
        }

        var deltaTime = 0
        var speedTarget = 0.125f
        fun animation(current: Float, targetAnimation: Float, speed: Float): Float {
            return animation(current, targetAnimation, speedTarget, speed)
        }

        fun animation(animation: Float, target: Float, poxyi: Float, speedTarget: Float): Float {
            var da =
                ((target - animation) / max(Minecraft.getDebugFPS().toFloat().toDouble(), 5.0) * 15.0f).toFloat()
            if (da > 0.0f) {
                da = max(speedTarget.toDouble(), da.toDouble()).toFloat()
                da = min((target - animation).toDouble(), da.toDouble()).toFloat()
            } else if (da < 0.0f) {
                da = min(-speedTarget.toDouble(), da.toDouble()).toFloat()
                da = max((target - animation).toDouble(), da.toDouble()).toFloat()
            }
            return animation + da
        }

        fun calculateCompensation(target: Float, current: Float, delta: Long, speed: Double): Float {
            var current = current
            var delta = delta
            val diff = current - target
            if (delta < 1) {
                delta = 1
            }
            if (delta > 1000) {
                delta = 16
            }
            if (diff > speed) {
                val xD = if (speed * delta / (1000 / 60) < 0.5) 0.5 else speed * delta / (1000 / 60)
                current -= xD.toFloat()
                if (current < target) {
                    current = target
                }
            } else if (diff < -speed) {
                val xD = if (speed * delta / (1000 / 60) < 0.5) 0.5 else speed * delta / (1000 / 60)
                current += xD.toFloat()
                if (current > target) {
                    current = target
                }
            } else {
                current = target
            }
            return current
        }
    }
}
