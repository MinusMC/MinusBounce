package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class OldAACNoFall: NoFallMode("OldAAC") {
	private var state = 0

	override fun onEnable() {
		state = 0
	}

	override fun onDisable() {
		state = 0
	}

	override fun onUpdate() {
		if (mc.thePlayer.fallDistance > 2f) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
            state = 2
        } else if (state == 2 && mc.thePlayer.fallDistance < 2f) {
            mc.thePlayer.motionY = 0.1
            state = 3
            return
        }

        if (state in 3..5) {
        	state++
            mc.thePlayer.motionY = 0.1
            if (state == 5)
            	state = 1
        }
	}
}