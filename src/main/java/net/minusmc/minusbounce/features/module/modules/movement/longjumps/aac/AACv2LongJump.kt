package net.minusmc.minusbounce.features.module.modules.movement.longjumps.aac

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.player.MovementUtils

class AACv2LongJump : LongJumpMode("AACv2") {
	override fun onUpdateJumped() {
		mc.thePlayer.jumpMovementFactor = 0.09F
        mc.thePlayer.motionY += 0.01321
        mc.thePlayer.jumpMovementFactor = 0.08F
        MovementUtils.strafe()
	}
}
