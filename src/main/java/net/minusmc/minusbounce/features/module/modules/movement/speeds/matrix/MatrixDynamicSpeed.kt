package net.minusmc.minusbounce.features.module.modules.movement.speeds.matrix

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.PreMotionEvent


class MatrixDynamicSpeed: SpeedMode("MatrixDynamic", SpeedType.MATRIX) {
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
            mc.timer.timerSpeed = 1.0f
            return
        }
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            return
        }
        if (mc.thePlayer.fallDistance <= 0.1) {
            mc.timer.timerSpeed = 1.9f
            return
        }
        if (mc.thePlayer.fallDistance < 1.3) {
            mc.timer.timerSpeed = 0.6f
            return
        }
        mc.timer.timerSpeed = 1.0f
    }

	override fun onPreMotion(event: PreMotionEvent) {
        onMotion()
    }

    override fun onPostMotion(event: PostMotionEvent) {
        onMotion()
    }
}