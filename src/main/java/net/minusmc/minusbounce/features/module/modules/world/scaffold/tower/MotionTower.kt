package net.minusmc.minusbounce.features.module.modules.world.scaffold.tower

import net.minusmc.minusbounce.features.module.modules.world.scaffold.TowerScaffold

class MotionTower: TowerScaffold("Motion") {
	override fun onPostMotion() {
		if (mc.thePlayer.onGround) {
            fakeJump()
            mc.thePlayer.motionY = 0.42
        } else if (mc.thePlayer.motionY < 0.1)
        	mc.thePlayer.motionY = -0.3
	}
}