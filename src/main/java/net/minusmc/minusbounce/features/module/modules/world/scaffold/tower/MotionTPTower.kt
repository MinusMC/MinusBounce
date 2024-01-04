package net.minusmc.minusbounce.features.module.modules.world.scaffold.tower

import net.minusmc.minusbounce.features.module.modules.world.scaffold.TowerScaffold
import kotlin.math.truncate

class MotionTPTower: TowerScaffold("MotionTP") {
	override fun onPostMotion() {
		if (mc.thePlayer.onGround) {
            fakeJump()
            mc.thePlayer.motionY = 0.41999998688698
        } else if (mc.thePlayer.motionY < 0.23) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, truncate(mc.thePlayer.posY), mc.thePlayer.posZ)
            mc.thePlayer.onGround = true
            mc.thePlayer.motionY = 0.41999998688698
        }
	}
}