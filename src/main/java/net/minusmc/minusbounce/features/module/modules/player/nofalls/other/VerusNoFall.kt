package net.minusmc.minusbounce.features.module.modules.player.nofalls.other

import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode

class VerusNoFall: NoFallMode("Verus") {
	private var canSpoof = false

	override fun onEnable() {
		canSpoof = false
	}

	override fun onDisable() {
		canSpoof = false
	}

	override fun onUpdate() {
		if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3F) {
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionX *= 0.5
            mc.thePlayer.motionX *= 0.5
            mc.thePlayer.fallDistance = 0F
            canSpoof = true
        }
	}

	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet
		if (packet is C03PacketPlayer && canSpoof) {
			packet.onGround = true
            canSpoof = false
		}
	}
}