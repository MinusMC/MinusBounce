package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.client.C0FPacketConfirmTransaction

class VulcanVelocity : VelocityMode("Vulcan") {

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (packet is C0FPacketConfirmTransaction) {
            val transUID = (packet.uid).toInt()

            if (transUID in -31767..-30769)
                event.isCancelled = true
        }
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId)
            event.isCancelled = true
    }
}