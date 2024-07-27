package net.minusmc.minusbounce.features.module.modules.player.nofalls.normal

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.SentPacketEvent

class SmartNoFall: NoFallMode("Smart") {
	private var packetModify = false
	private var lastFallDistRounded = 0

	override fun onEnable() {
		packetModify = false
		lastFallDistRounded = 0
	}

	override fun onDisable() {
		packetModify = false
		lastFallDistRounded = 0
	}

	override fun onUpdate() {
		if (mc.thePlayer.fallDistance.toInt() / 3 > lastFallDistRounded) {
			lastFallDistRounded = mc.thePlayer.fallDistance.toInt() / 3
			packetModify = true
		}
		if (mc.thePlayer.onGround)
			lastFallDistRounded = 0
	}

	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet
		if (packet is C03PacketPlayer && packetModify) {
			packet.onGround = true
			packetModify = false
		}
	}
}