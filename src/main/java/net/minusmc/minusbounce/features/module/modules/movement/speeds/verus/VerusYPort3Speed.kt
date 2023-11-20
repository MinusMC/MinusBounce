package net.minusmc.minusbounce.features.module.modules.movement.speeds.verus

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils

class VerusYPort3Speed: SpeedMode("VerusYPort3", SpeedType.VERUS) {
    private var verusTick = 0

    override fun onDisable() {
        verusTick = 0
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving && mc.thePlayer.onGround && verusTick == 0) {
            mc.thePlayer.motionY = 0.42
            verusTick = 1
        } else if (verusTick == 1) {
            verusTick = 2
            MovementUtils.strafe(0.2f)
        } else if (verusTick == 5) {
            verusTick = 0
        } else if (verusTick < 5) {
            verusTick++
            mc.thePlayer.motionY -= 0.12
        }
    }
}