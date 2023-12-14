package net.minusmc.minusbounce.features.module.modules.movement.speeds.verus

import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.value.FloatValue

class VerusYPort2Speed : SpeedMode("VerusYPort2", SpeedType.VERUS) {
	private val speedValue = FloatValue("Speed", 0.61f, 0.1f, 1f)

	override fun onMove(event: MoveEvent) {
		if (MovementUtils.isMoving) {
			mc.gameSettings.keyBindJump.pressed = false
			if (mc.thePlayer.onGround) {
				MovementUtils.strafe(speedValue.get())
				event.y = 0.41999998688698
			} else {
				MovementUtils.strafe()
			}
		}
	}

	override fun onUpdate() {
		mc.thePlayer.motionY = -0.0784000015258789
	}
}