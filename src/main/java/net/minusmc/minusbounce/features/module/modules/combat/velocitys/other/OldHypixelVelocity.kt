package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class OldHypixelVelocity : VelocityMode("OldHypixel") {
    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
              event.isCancelled = true
              mc.thePlayer.motionY = packet.motionY.toDouble() / 8000.0
        }
    }
}