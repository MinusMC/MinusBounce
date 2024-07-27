package net.minusmc.minusbounce.features.module.modules.movement.longjumps.aac

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.event.JumpEvent

class AACv4LongJump : LongJumpMode("AACv4") {
	override fun onUpdateJumped() {
		mc.thePlayer.jumpMovementFactor = 0.05837456f
        mc.timer.timerSpeed = 0.5f
	}

	override fun onJump(event: JumpEvent) {
        if (longjump.state) event.motion *= 1.0799f
	}
}
