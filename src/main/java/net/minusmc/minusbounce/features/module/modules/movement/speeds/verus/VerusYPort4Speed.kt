package net.minusmc.minusbounce.features.module.modules.movement.speeds.verus

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils

class VerusYPort4Speed: SpeedMode("VerusYPort4", SpeedType.VERUS){
    private var doSpeed = false
    override fun onDisable() {
        doSpeed = false
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving && mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42
            MovementUtils.strafe()
            doSpeed = true
        } else if (doSpeed) {
            doSpeed = false
            mc.thePlayer.motionY -= 0.25
        }
    }
}