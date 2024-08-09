package net.minusmc.minusbounce.features.module.modules.movement

import net.minecraft.client.settings.GameSettings
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent

@ModuleInfo(name = "AutoWalk", spacedName = "Auto Walk", description = "Walk forward automatically.", category = ModuleCategory.MOVEMENT)
class AutoWalk: Module() {

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		mc.gameSettings.keyBindForward.pressed = true
	}

	override fun onDisable() {
		mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
	}
}