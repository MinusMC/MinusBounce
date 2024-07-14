package net.minusmc.minusbounce.features.module.modules.world.scaffold.tower

import net.minusmc.minusbounce.features.module.modules.world.scaffold.TowerScaffold

class UniversocraftTower: TowerScaffold("Universocraft") {
	override fun onPostMotion() {
		if (mc.thePlayer.onGround) {
            fakeJump()
            mc.thePlayer.motionY = 0.4001
        }
        mc.timer.timerSpeed = 1f
        if (mc.thePlayer.motionY < 0) {
            mc.thePlayer.motionY -= 0.00000945
            mc.timer.timerSpeed = 1.6f
        }
	}
}