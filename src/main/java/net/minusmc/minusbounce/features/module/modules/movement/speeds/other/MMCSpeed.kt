package net.minusmc.minusbounce.features.module.modules.movement.speeds.other

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.value.BoolValue

class MMCSpeed: SpeedMode("MMC", SpeedType.OTHER) {

    private val veloAbuseValue = BoolValue("Abuse", false)

    override fun onPreMotion() {
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.hurtTime < 6 || veloAbuseValue.get()) {
                MovementUtils.strafe()
            }
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                MovementUtils.strafe()
            }
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}