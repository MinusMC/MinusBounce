package net.minusmc.minusbounce.features.module.modules.movement.longjumps.mineplex

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.event.MoveEvent

class Mineplex3LongJump : LongJumpMode("Mineplex3") {
	override fun onUpdate() {
		mc.thePlayer.jumpMovementFactor = 0.09f
        mc.thePlayer.motionY += 0.01321
        mc.thePlayer.jumpMovementFactor = 0.08f
        MovementUtils.strafe()
	}

	override fun onMove(event: MoveEvent) {
		if (mc.thePlayer.fallDistance != 0f)
			mc.thePlayer.motionY += 0.037
	}
}
