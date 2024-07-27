package net.minusmc.minusbounce.features.module.modules.player.nofalls.normal

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.SentPacketEvent

class DamageNoFall: NoFallMode("Damage") {
	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet
		if (packet is C03PacketPlayer && mc.thePlayer.fallDistance > 3.5f)
			packet.onGround = true
	}
}