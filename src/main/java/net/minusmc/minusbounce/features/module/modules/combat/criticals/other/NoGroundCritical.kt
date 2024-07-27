package net.minusmc.minusbounce.features.module.modules.combat.criticals.other

import net.minusmc.minusbounce.features.module.modules.combat.criticals.CriticalMode
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minecraft.network.play.client.C03PacketPlayer

class NoGroundCritical : CriticalMode("NoGround") {
	override fun onEnable() {
        mc.thePlayer.jump()
	}

	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet
		if (packet is C03PacketPlayer)
            packet.onGround = false
	}
}
