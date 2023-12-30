package net.minusmc.minusbounce.features.module.modules.player.antivoids.normal

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer


class MotionFlagAntiVoid: AntiVoidMode("MotionFlag") {
    private val motionflagValue = FloatValue("MotionY", 1.0F, 0.0F, 5.0F)
    
    private var tried = false

    override fun onEnable() {
        tried = false
    }

    override fun onUpdate() {
        if (!antivoid.voidOnlyValue.get() || isVoid) {
            if (mc.thePlayer.fallDistance > antivoid.maxFallDistValue.get() && !tried) {
                mc.thePlayer.motionY += motionflagValue.get()
                mc.thePlayer.fallDistance = 0.0F
                tried = true
            }
        }
    }
}