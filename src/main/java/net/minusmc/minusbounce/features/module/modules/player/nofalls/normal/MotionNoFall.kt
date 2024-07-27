package net.minusmc.minusbounce.features.module.modules.player.nofalls.normal

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.value.FloatValue

class MotionNoFall: NoFallMode("Motion") {
	private val flySpeedValue = FloatValue("MotionSpeed", -0.01F, -5F, 5F)

	override fun onUpdate() {
		if (mc.thePlayer.fallDistance > 3F)
			mc.thePlayer.motionY = flySpeedValue.get().toDouble()
	}
}