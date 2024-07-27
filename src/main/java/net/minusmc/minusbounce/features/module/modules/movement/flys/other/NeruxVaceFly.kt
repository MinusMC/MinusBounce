package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.value.IntegerValue

class NeruxVaceFly: FlyMode("NeruxVace", FlyType.OTHER) {
	private val neruxVaceTicks = IntegerValue("Ticks", 6, 0, 20)
	private var ticks = 0

	override fun onUpdate() {
		if (!mc.thePlayer.onGround)
            ticks++

        if (ticks >= neruxVaceTicks.get() && !mc.thePlayer.onGround) {
            ticks = 0
            mc.thePlayer.motionY = 0.015
        }
	}
}