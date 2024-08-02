package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent

@ModuleInfo(name = "AutoJump", spacedName = "Auto Jump", description = "Jumps automatically.", category = ModuleCategory.MOVEMENT)
class AutoJump: Module() {
	
	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed) 
			mc.thePlayer.jump()
	}
}