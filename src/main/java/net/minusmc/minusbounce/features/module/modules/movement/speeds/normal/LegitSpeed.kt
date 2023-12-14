package net.minusmc.minusbounce.features.module.modules.movement.speeds.legit

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.value.BoolValue

class LegitSpeed: SpeedMode("Legit", SpeedType.NORMAL) {

    private val cpuSPEED = BoolValue("CPU SpeedUp Exploit", true)
    private val jumpSpeed = BoolValue("No Jump Delay", true)

    override fun onUpdate() {
        if (cpuSPEED.get()) mc.timer.timerSpeed = 1.004f
        if (jumpSpeed.get()) mc.thePlayer.jumpTicks = 0
        if (mc.thePlayer.isInWater) return
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.onGround) mc.thePlayer.jump()
        }
    }
}