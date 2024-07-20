package net.minusmc.minusbounce.features.module.modules.movement.nowebs.other

import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode

class RewiNoWeb: NoWebMode("Rewi") {
	override fun onUpdate() {
        if (!mc.thePlayer.isInWeb)
            return

		mc.thePlayer.jumpMovementFactor = 0.42f

        if (mc.thePlayer.onGround)
            mc.thePlayer.jump()
    }
}
