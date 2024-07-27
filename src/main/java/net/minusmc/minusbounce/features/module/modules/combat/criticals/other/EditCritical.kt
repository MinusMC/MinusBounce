package net.minusmc.minusbounce.features.module.modules.combat.criticals.other


import net.minusmc.minusbounce.features.module.modules.combat.criticals.CriticalMode
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.event.AttackEvent
import net.minecraft.network.play.client.C03PacketPlayer


class EditCritical : CriticalMode("Edit") {
    private var readyCrits = false

    override fun onAttack(event: AttackEvent) {
        readyCrits = true
    }
	override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet
		if (readyCrits) {
            if (packet is C03PacketPlayer)
                packet.onGround = false
            readyCrits = false
        }
	}
}
