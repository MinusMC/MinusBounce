package net.minusmc.minusbounce.ui.client.clickgui

import net.minusmc.minusbounce.utils.AnimationHelper

class Opacity(opacity: Int) { // uk chac la lay tu dau v lq+ reborned
    var opacity: Float
    private var lastMS: Long

    init {
        this.opacity = opacity.toFloat()
        lastMS = System.currentTimeMillis()
    }

    fun interpolate(targetOpacity: Float) {
        val currentMS = System.currentTimeMillis()
        val delta = currentMS - lastMS
        lastMS = currentMS
        opacity = AnimationHelper.calculateCompensation(targetOpacity, opacity, delta, 20)
    }

    fun interp(targetOpacity: Float, speed: Int) {
        val currentMS = System.currentTimeMillis()
        val delta = currentMS - lastMS
        lastMS = currentMS
        opacity = AnimationHelper.calculateCompensation(targetOpacity, opacity, delta, speed)
    }
}