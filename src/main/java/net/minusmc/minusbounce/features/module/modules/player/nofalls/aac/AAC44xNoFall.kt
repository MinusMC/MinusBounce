package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.*

class AAC44xNoFall: NoFallMode("AAC4.4.x") {
    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && mc.thePlayer.fallDistance > 1.6)
            packet.onGround = true
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity && mc.thePlayer.fallDistance > 1.8)
            packet.motionY /= -10
    }
}