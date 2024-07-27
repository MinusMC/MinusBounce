package net.minusmc.minusbounce.features.module.modules.player.nofalls.hypixel

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.SentPacketEvent

class HypixelNewNoFall: NoFallMode("HypixelNew") {
	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet
		if (mc.thePlayer.fallDistance > 2.5F && mc.thePlayer.ticksExisted % 2 == 0 && packet is C03PacketPlayer) {
            packet.onGround = true
			packet.isMoving = false
        }
	}
}