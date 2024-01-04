package net.minusmc.minusbounce.features.module.modules.movement.speeds.other

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.utils.MovementUtils

class GroundStrafeHopSpeed: SpeedMode("GroundStrafeHop", SpeedType.OTHER) {
    override fun onMotion(event: MotionEvent) {
        if (event.eventState != EventState.PRE) return
        if (MovementUtils.isMoving) {
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