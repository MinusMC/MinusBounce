package net.minusmc.minusbounce.features.module.modules.movement.speeds.aac

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.event.PostMotionEvent

class AACv4BHopSpeed: SpeedMode("AACv4BHop", SpeedType.AAC) {
	override fun onPostMotion(event: PostMotionEvent) {
        if (mc.thePlayer.isInWater) return

        if (mc.thePlayer.moveForward > 0) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mc.timer.timerSpeed = 1.6105f
                mc.thePlayer.motionX *= 1.0708
                mc.thePlayer.motionZ *= 1.0708
            } else if (mc.thePlayer.fallDistance > 0) {
                mc.timer.timerSpeed = 0.6f
            }               
        }    
	}
}
