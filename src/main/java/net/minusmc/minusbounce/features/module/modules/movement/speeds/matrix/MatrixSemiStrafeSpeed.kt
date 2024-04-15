package net.minusmc.minusbounce.features.module.modules.movement.speeds.matrix

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.PreMotionEvent


class MatrixSemiStrafeSpeed: SpeedMode("MatrixSemiStrafe", SpeedType.MATRIX) {
	override fun onEnable() {
		mc.thePlayer.jumpMovementFactor = 0.02f
		mc.timer.timerSpeed = 1f
	}
	override fun onDisable() {
		mc.thePlayer.jumpMovementFactor = 0.02f
        mc.timer.timerSpeed = 1.0f
	}
	fun onMotion() {
		if (MovementUtils.isMoving && mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            MovementUtils.strafe(0.3f)
        }
        if (mc.thePlayer.fallDistance > 0.1) {
            MovementUtils.strafe(0.22f)
        }
	}

	override fun onPreMotion(event: PreMotionEvent) {
        onMotion()
    }

    override fun onPostMotion(event: PostMotionEvent) {
        onMotion()
    }
}