package net.minusmc.minusbounce.features.module.modules.movement.longjumps.mineplex

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.event.JumpEvent

class MineplexLongJump : LongJumpMode("Mineplex") {
	override fun onUpdate() {
        mc.thePlayer.motionY += 0.01321
        mc.thePlayer.jumpMovementFactor = 0.08F
        MovementUtils.strafe()
	}

	override fun onJump(event: JumpEvent) {
        if (longjump.state)
        	event.motion *= 4.08f
	}
}
