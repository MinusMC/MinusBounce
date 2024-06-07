package net.minusmc.minusbounce.features.module.modules.movement.nowebs.aac

import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode

class AACNoWeb: NoWebMode("AAC") {
	override fun onUpdate() {
		mc.thePlayer.jumpMovementFactor = 0.59f
		
        if (!mc.gameSettings.keyBindSneak.isKeyDown) 
        	mc.thePlayer.motionY = 0.0
	}
}