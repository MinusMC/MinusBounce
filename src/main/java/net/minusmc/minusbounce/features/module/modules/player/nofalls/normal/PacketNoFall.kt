package net.minusmc.minusbounce.features.module.modules.player.nofalls.normal

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class PacketNoFall: NoFallMode("Packet") {
	override fun onUpdate() {
		if (mc.thePlayer.fallDistance > 2F) 
			mc.netHandler.addToSendQueue(C03PacketPlayer(true))
	}
}