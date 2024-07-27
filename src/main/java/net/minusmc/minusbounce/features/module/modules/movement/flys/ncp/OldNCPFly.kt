package net.minusmc.minusbounce.features.module.modules.movement.flys.ncp

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.player.MovementUtils


class OldNCPFly: FlyMode("OldNCP", FlyType.NCP) {
	override fun onUpdate() {
		if (startY > mc.thePlayer.posY)
            mc.thePlayer.motionY = -0.000000000000000000000000000000001

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY = -0.2

        if (mc.gameSettings.keyBindJump.isKeyDown && mc.thePlayer.posY < startY - 0.1)
            mc.thePlayer.motionY = 0.2

        MovementUtils.strafe()
	}
}