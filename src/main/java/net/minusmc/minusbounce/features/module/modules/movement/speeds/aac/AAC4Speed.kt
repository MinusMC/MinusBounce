package net.minusmc.minusbounce.features.module.modules.movement.speeds.aac

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils

class AAC4Speed : SpeedMode("AAC4", SpeedType.AAC) {
    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer!!.speedInAir = 0.02f
    }
    override fun onUpdate() {
        mc.timer.timerSpeed = 1.00f
        if (!MovementUtils.isMoving) return

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            mc.timer.timerSpeed = 1.00f
        }
        if (mc.thePlayer.fallDistance > 0.7 && mc.thePlayer.fallDistance < 1.3) {
            mc.timer.timerSpeed = 1.08f
        }
    }
}
