package net.minusmc.minusbounce.features.module.modules.movement.flys.aac

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.value.BoolValue


class AAC305Fly: FlyMode("AAC3.0.5", FlyType.AAC) {
	private val aacFast = BoolValue("Fast", true)
	private var aac3delay = 0

	override fun onUpdate() {
		if (aac3delay == 2)
            mc.thePlayer.motionY = 0.1
        else if (aac3delay > 2)
            aac3delay = 0

        if (aacFast.get()) {
            if (mc.thePlayer.movementInput.moveStrafe == 0f)
                mc.thePlayer.jumpMovementFactor = 0.08f
            else
                mc.thePlayer.jumpMovementFactor = 0f
        }

        aac3delay++
	}
}