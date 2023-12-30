package net.minusmc.minusbounce.features.module.modules.player.antivoids.other

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook


class OldHypixelAntiVoid: AntiVoidMode("OldHypixel") {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook && mc.thePlayer.fallDistance > 3.125)
            mc.thePlayer.fallDistance = 3.125f

        if (packet is C03PacketPlayer) {
            if (antivoid.voidOnlyValue.get() && mc.thePlayer.fallDistance >= antivoid.maxFallDistValue.get() && mc.thePlayer.motionY <= 0 && isVoid) {
                packet.y += 11.0
            }
            if (!antivoid.voidOnlyValue.get() && mc.thePlayer.fallDistance >= antivoid.maxFallDistValue.get()) packet.y += 11.0
        }
    }
}