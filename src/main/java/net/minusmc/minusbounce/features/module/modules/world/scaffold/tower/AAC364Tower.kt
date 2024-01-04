package net.minusmc.minusbounce.features.module.modules.world.scaffold.tower

import net.minusmc.minusbounce.features.module.modules.world.scaffold.TowerScaffold

class AAC364Tower: TowerScaffold("AAC3.6.4") {
	override fun onPostMotion() {
		if (mc.thePlayer.ticksExisted % 4 == 1) {
            mc.thePlayer.motionY = 0.4195464
            mc.thePlayer.setPosition(mc.thePlayer.posX - 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
        } else if (mc.thePlayer.ticksExisted % 4 == 0) {
            mc.thePlayer.motionY = -0.5
            mc.thePlayer.setPosition(mc.thePlayer.posX + 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
        }
	}
}