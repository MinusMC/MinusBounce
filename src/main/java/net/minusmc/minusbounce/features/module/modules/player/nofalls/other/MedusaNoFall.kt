package net.minusmc.minusbounce.features.module.modules.player.nofalls.other

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.utils.PacketUtils

class MedusaNoFall: NoFallMode("Medusa") {
    override fun onSentPacket(event: SentPacketEvent) {
        if (mc.thePlayer.fallDistance > 2.3F) {
            event.isCancelled = true
            PacketUtils.sendPacketNoEvent(C03PacketPlayer(true))
            mc.thePlayer.fallDistance = 0F
        }
    }
}