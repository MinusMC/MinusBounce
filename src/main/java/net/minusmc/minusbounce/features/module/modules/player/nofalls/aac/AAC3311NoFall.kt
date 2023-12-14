package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class AAC3311NoFall: NoFallMode("AAC 3.3.11") {
	override fun onUpdate() {
		if (mc.thePlayer.fallDistance > 2F) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 10E-4, mc.thePlayer.posZ, mc.thePlayer.onGround))
            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
        }
	}
}