package net.minusmc.minusbounce.features.module.modules.movement.speeds.intave

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class IntaveSpeed: SpeedMode("Intave", SpeedType.INTAVE) {
    var offGroundTicks = 0

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