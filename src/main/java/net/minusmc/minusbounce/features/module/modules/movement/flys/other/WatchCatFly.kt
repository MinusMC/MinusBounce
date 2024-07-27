package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.player.MovementUtils

class WatchCatFly: FlyMode("WatchCat", FlyType.OTHER) {
	override fun onUpdate() {
		MovementUtils.strafe(0.15f)
        mc.thePlayer.isSprinting = true

        if (mc.thePlayer.posY < startY + 2) {
            mc.thePlayer.motionY = Math.random() * 0.5
            return
        }

        if (startY > mc.thePlayer.posY)
            MovementUtils.strafe(0f)
	}
}