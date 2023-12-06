package net.minusmc.minusbounce.features.module.modules.movement.longjumps.mineplex

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.event.JumpEvent

class Mineplex2LongJump : LongJumpMode("Mineplex2") {
	private var canMineplexBoost = false

	override fun onEnable() {
		canMineplexBoost = false
	}

	override fun onUpdate() {
        if (mc.thePlayer.onGround || mc.thePlayer.capabilities.isFlying) {
            canMineplexBoost = false
            return
        }

		if (!canMineplexBoost) return

        mc.thePlayer.jumpMovementFactor = 0.1F

        if (mc.thePlayer.fallDistance > 1.5F) {
            mc.thePlayer.jumpMovementFactor = 0F
            mc.thePlayer.motionY = -10.0
        }
        MovementUtils.strafe()
	}

	override fun onJump(event: JumpEvent) {
		if (!longjump.state) return
		if (mc.thePlayer.isCollidedHorizontally) {
            event.motion = 2.31f
            canMineplexBoost = true
            mc.thePlayer.onGround = false
        }
	}
}
