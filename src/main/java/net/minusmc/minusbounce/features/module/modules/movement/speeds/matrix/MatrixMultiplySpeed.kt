package net.minusmc.minusbounce.features.module.modules.movement.speeds.matrix

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.PreMotionEvent


class MatrixMultiplySpeed: SpeedMode("MatrixMultiply", SpeedType.MATRIX) {
	override fun onEnable() {
		mc.thePlayer.jumpMovementFactor = 0.02f
		mc.timer.timerSpeed = 1f
	}
	override fun onDisable() {
		mc.thePlayer.jumpMovementFactor = 0.02f
        mc.timer.timerSpeed = 1.0f
	}
	fun onMotion() {
		if (!MovementUtils.isMoving) {
            return
        }
        if (mc.thePlayer.onGround) {
            mc.timer.timerSpeed = 1.0f
            mc.thePlayer.jump()
        }
        if (mc.thePlayer.motionY > 0.003) {
            mc.thePlayer.motionX *= 1.0012
            mc.thePlayer.motionZ *= 1.0012
            mc.timer.timerSpeed = 1.05f
        }
	}

	override fun onPreMotion(event: PreMotionEvent) {
        onMotion()
    }

    override fun onPostMotion(event: PostMotionEvent) {
        onMotion()
    }
}