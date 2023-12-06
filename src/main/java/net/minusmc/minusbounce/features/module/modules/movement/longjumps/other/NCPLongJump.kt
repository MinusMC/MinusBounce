package net.minusmc.minusbounce.features.module.modules.movement.longjumps.other

import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.event.MoveEvent

class NCPLongJump : LongJumpMode("NCP") {
	private val ncpBoostValue = FloatValue("Boost", 4.25F, 1F, 10F)
	private var canBoost = false

	override fun resetMotion() {
		mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
	}

	override fun onUpdate() {
		if (mc.thePlayer.onGround || mc.thePlayer.capabilities.isFlying) {
			mc.thePlayer.motionX = 0.0
			mc.thePlayer.motionZ = 0.0
			return
		}

		val boost = if (canBoost) ncpBoostValue.get() else 1f
		MovementUtils.strafe(MovementUtils.speed * boost)
        canBoost = false
	}

	override fun onMove(event: MoveEvent) {
		if (!MovementUtils.isMoving && longjump.jumped) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            event.zeroXZ()
        }
	}

	override fun onJump(event: JumpEvent) {
		canBoost = true
	}
}
