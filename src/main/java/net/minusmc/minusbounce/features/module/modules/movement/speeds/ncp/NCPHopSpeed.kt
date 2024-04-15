package net.minusmc.minusbounce.features.module.modules.movement.speeds.ncp

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.player.MovementUtils

class NCPHopSpeed: SpeedMode("NCPHop", SpeedType.NCP) {
	override fun onEnable() {
        mc.timer.timerSpeed = 1.0865f
    }

    override fun onDisable() {
        mc.thePlayer.speedInAir = 0.02f
        mc.timer.timerSpeed = 1f
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mc.thePlayer.speedInAir = 0.0223f
            }
            MovementUtils.strafe()
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
