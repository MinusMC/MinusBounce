package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.event.MoveEvent

class LAACNoFall: NoFallMode("LAAC") {
	private var jumped = false

	override fun onUpdate() {
		if (mc.thePlayer.onGround) jumped = false
        if (mc.thePlayer.motionY > 0) jumped = true

		if (!jumped && mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInWeb) {
            mc.thePlayer.motionY = -6.0
        }
	}

	override fun onMove(event: MoveEvent) {
		if (!jumped && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInWeb && mc.thePlayer.motionY < 0.0) {
            event.x = 0.0
            event.z = 0.0
        }
	}

	override fun onJump() {
		jumped = true
	}
}