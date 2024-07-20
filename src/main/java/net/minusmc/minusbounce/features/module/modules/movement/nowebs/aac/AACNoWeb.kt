package net.minusmc.minusbounce.features.module.modules.movement.nowebs.aac

import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode

class AACNoWeb: NoWebMode("AAC") {
	override fun onUpdate() {
		if (!mc.thePlayer.isInWeb)
			return

		mc.thePlayer.jumpMovementFactor = 0.59f
		
        if (!mc.gameSettings.keyBindSneak.isKeyDown) 
        	mc.thePlayer.motionY = 0.0
	}
}