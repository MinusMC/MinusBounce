/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement.speeds.aac

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils.isMoving

class AAC4SlowHopSpeed: SpeedMode("AAC4SlowHop", SpeedType.AAC) {
    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer!!.speedInAir = 0.02f
    }
    override fun onUpdate() {
        if (mc.thePlayer!!.isInWater) return

        if (isMoving) {
            if (mc.thePlayer!!.onGround) {
                mc.gameSettings.keyBindJump.pressed = false
                mc.thePlayer!!.jump()
            }
            if (!mc.thePlayer!!.onGround && mc.thePlayer!!.fallDistance <= 0.1) {
                mc.thePlayer!!.speedInAir = 0.02f
                mc.timer.timerSpeed = 1.4f
            }
            if (mc.thePlayer!!.fallDistance > 0.1 && mc.thePlayer!!.fallDistance < 1.3) {
                mc.thePlayer!!.speedInAir = 0.0205f
                mc.timer.timerSpeed = 0.65f
            }
            if (mc.thePlayer!!.fallDistance >= 1.3) {
                mc.timer.timerSpeed = 1f
                mc.thePlayer!!.speedInAir = 0.02f
            }
        } else {
            mc.thePlayer!!.motionX = 0.0
            mc.thePlayer!!.motionZ = 0.0
        }
    }
}
