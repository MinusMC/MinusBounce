package net.minusmc.minusbounce.features.module.modules.player.antivoids.other

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.event.*
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook


class OldHypixelAntiVoid: AntiVoidMode("OldHypixel") {
    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook && mc.thePlayer.fallDistance > 3.125)
            mc.thePlayer.fallDistance = 3.125f

    }

     override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && mc.thePlayer.fallDistance >= antivoid.maxFallDistValue.get())
            if (!antivoid.voidOnlyValue.get() || (mc.thePlayer.motionY <= 0 && isVoid))
                packet.y += 11.0
    }
}