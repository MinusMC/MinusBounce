package net.minusmc.minusbounce.features.module.modules.movement.speeds.intave

import net.minusmc.MinusBounce.LiquidBounce
import net.minusmc.MinusBounce.event.MotionEvent
import net.minusmc.MinusBounce.features.module.modules.movement.Speed
import net.minusmc.MinusBounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.MinusBounce.utils.MovementUtils

class IntaveSpeed : SpeedMode("Intave") {
    var offGroundTicks = 0
    val speed = LiquidBounce.moduleManager.getModule(Speed::class.java)

    override fun onMotion(event: MotionEvent) {
        if(!mc.thePlayer.onGround)
            offGroundTicks++
        else offGroundTicks = 0

        if(mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }

        if (offGroundTicks >= 10) {
            MovementUtils.setMoveSpeed(MovementUtils.getSpeed().toDouble())
        }

        if (mc.thePlayer.motionY > 0.003) {
            mc.thePlayer.motionX *= 1.0015
            mc.thePlayer.motionZ *= 1.0015
            mc.timer.timerSpeed = 1.1f
        }
    }
}