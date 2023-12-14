package net.minusmc.minusbounce.features.module.modules.movement.speeds.aac

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils.isMoving

class AAC4HopSpeed: SpeedMode("AAC4Hop", SpeedType.AAC) {
    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer!!.speedInAir = 0.02f
    }
    override fun onUpdate() {
        if (mc.thePlayer!!.isInWater) return

        if (isMoving) {
            if (mc.thePlayer!!.onGround) {
                mc.thePlayer!!.jump()
                mc.thePlayer!!.speedInAir = 0.0201f
                mc.timer.timerSpeed = 0.94f
            }
            if (mc.thePlayer!!.fallDistance > 0.7 && mc.thePlayer!!.fallDistance < 1.3) {
                mc.thePlayer!!.speedInAir = 0.02f
                mc.timer.timerSpeed = 1.8f
            }
            if (mc.thePlayer!!.fallDistance >= 1.3){
                mc.timer.timerSpeed = 1f
                mc.thePlayer!!.speedInAir = 0.02f
            }
        } else {
            mc.thePlayer!!.motionX = 0.0
            mc.thePlayer!!.motionZ = 0.0
        }
    }
}
