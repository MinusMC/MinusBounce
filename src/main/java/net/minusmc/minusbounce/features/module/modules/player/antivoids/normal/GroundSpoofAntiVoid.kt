package net.minusmc.minusbounce.features.module.modules.player.antivoids.normal

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minecraft.network.play.client.C03PacketPlayer


class GroundSpoofAntiVoid: AntiVoidMode("GroundSpoof") {
    private var canSpoof = false

    override fun onEnable() {
        canSpoof = false
    }

    override fun onUpdateVoided() {
        canSpoof = mc.thePlayer.fallDistance > antivoid.maxFallDistValue.get()
    }

    override fun onSentPacket(event: SentPacketEvent) {
    	val packet = event.packet
    	if (canSpoof && packet is C03PacketPlayer)
            packet.onGround = true
    }
}