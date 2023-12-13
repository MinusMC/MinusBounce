package net.minusmc.minusbounce.utils.render.animations.impl

import net.minusmc.minusbounce.utils.render.animations.Animation
import net.minusmc.minusbounce.utils.render.animations.Direction

open class CustomAnimation(duration: Int, target: Double, direction: Direction, val easeFunc: (Double) -> Double): Animation(duration, target, direction) {
    override fun getEquation(x: Double) = easeFunc(x)
}