package net.minusmc.minusbounce.features.module.modules.movement.speeds.other

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.value.BoolValue

class LegitSpeed: SpeedMode("Legit", SpeedType.OTHER) {

    private val cpuSPEED = BoolValue("Timer-Bypass", true)

    override fun onUpdate() {
        if (cpuSPEED.get()) mc.timer.timerSpeed = 1.004f
        if (mc.thePlayer.isInWater) return
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.onGround) mc.thePlayer.jump()
        }
    }
}