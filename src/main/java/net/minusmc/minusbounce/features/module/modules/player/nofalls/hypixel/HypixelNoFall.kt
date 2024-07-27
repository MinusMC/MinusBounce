package net.minusmc.minusbounce.features.module.modules.player.nofalls.hypixel

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.SentPacketEvent

class HypixelNoFall: NoFallMode("Hypixel") {
	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet
		if (packet is C03PacketPlayer && mc.thePlayer.fallDistance > 1.5f)
			packet.onGround = mc.thePlayer.ticksExisted % 2 == 0
	}
}