package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class OldMinemenVelocity : VelocityMode("OldMinemen") {

    private var counter = 0

    override fun onPreMotion(event: PreMotionEvent) {
        counter++
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        var packet = event.packet

        if (counter <= 20)
            return

        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
            event.isCancelled = true
            counter = 0
        }

        if (packet is S27PacketExplosion) {
            event.isCancelled = true
            counter = 0
        }
    }

}