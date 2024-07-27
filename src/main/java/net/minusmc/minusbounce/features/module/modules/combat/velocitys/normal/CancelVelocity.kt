package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity

class CancelVelocity : VelocityMode("Cancel") {
    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (event.packet is S12PacketEntityVelocity)
            event.isCancelled = true
    }
}