package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class OldAACNoFall: NoFallMode("OldAAC") {
	private var oldaacState = 0

	override fun onEnable() {
		oldaacState = 0
	}

	override fun onDisable() {
		oldaacState = 0
	}

	override fun onUpdate() {
		if (mc.thePlayer.fallDistance > 2f) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
            oldaacState = 2
        } else if (oldaacState == 2 && mc.thePlayer.fallDistance < 2) {
            mc.thePlayer.motionY = 0.1
            oldaacState = 3
            return
        }
        if (oldaacState in 3..5) {
            mc.thePlayer.motionY = 0.1
            if (oldaacState == 5) oldaacState = 1
        }
	}
}