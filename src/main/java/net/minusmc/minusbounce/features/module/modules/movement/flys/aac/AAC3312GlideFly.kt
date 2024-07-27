package net.minusmc.minusbounce.features.module.modules.movement.flys.aac

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType

class AAC3312GlideFly: FlyMode("AAC3.3.12-Glide", FlyType.AAC) {
	private var aac3glideDelay = 0

	override fun onUpdate() {
		if (!mc.thePlayer.onGround)
            aac3glideDelay++

        if (aac3glideDelay == 2)
            mc.timer.timerSpeed = 1f

        if (aac3glideDelay == 12)
            mc.timer.timerSpeed = 0.1f

        if (aac3glideDelay >= 12 && !mc.thePlayer.onGround) {
            aac3glideDelay = 0
            mc.thePlayer.motionY = 0.015
        }
	}
}