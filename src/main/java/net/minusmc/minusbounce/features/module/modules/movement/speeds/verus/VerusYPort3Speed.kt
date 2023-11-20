package net.minusmc.minusbounce.features.module.modules.movement.speeds.verus

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils

class VerusYPort3Speed: SpeedMode("VerusYPort3", SpeedType.VERUS) {
    private var verusTick = 0

    override fun onDisable() {
        verusTick = 0
        mc.timer.timerSpeed = 1f
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.onGround && verusTick == 0) {
                mc.thePlayer.jump()
                verusTick = 1
            } else if (verusTick == 1) {
                verusTick = 2
                MovementUtils.strafe()
            } else if (verusTick == 5) {
                verusTick = 0
                mc.timer.timerSpeed = 1f
            } else if (verusTick < 5) {
                verusTick++
                mc.timer.timerSpeed = 1.05f
                if (mc.thePlayer.ticksExisted % 3 == 0) {
                    mc.thePlayer.motionY -= 0.16
                } else {
                    mc.thePlayer.motionY -= 0.11
                }
            }
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}