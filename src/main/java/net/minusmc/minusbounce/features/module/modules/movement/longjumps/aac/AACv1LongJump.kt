package net.minusmc.minusbounce.features.module.modules.movement.longjumps.aac

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.player.MovementUtils

class AACv1LongJump : LongJumpMode("AACv1") {
	override fun onUpdateJumped() {
		mc.thePlayer.motionY += 0.05999
    	MovementUtils.strafe(MovementUtils.speed * 1.08F)
	}
}
