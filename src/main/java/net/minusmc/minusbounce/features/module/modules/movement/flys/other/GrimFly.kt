package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.network.play.server.S27PacketExplosion

class GrimFly: FlyMode("Grim", FlyType.OTHER) {
    private var velocityPacket = false

    override fun onEnable() {
		super.onEnable()
        velocityPacket = false
    }

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet
        
        if (velocityPacket && packet is C03PacketPlayer) {
            packet.x = mc.thePlayer.posX + 1000.0
            packet.z = mc.thePlayer.posZ + 1000.0
        }
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (event.packet is S27PacketExplosion)
            velocityPacket = true
    }
}