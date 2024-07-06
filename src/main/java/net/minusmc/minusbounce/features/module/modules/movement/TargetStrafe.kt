package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.*

@ModuleInfo(name = "TargetStrafe", spacedName = "Target Strafe", description = "Strafe around your target.", category = ModuleCategory.MOVEMENT)
class TargetStrafe: Module() {

	private val rangeValue = FloatValue("Range", 0f, 0f, 6f, "m")
	private val modeValue = ListValue("Mode", arrayOf("Normal", "Legit"), "Normal")
	private val holdJumpValue = BoolValue("HoldJump", true)

}