package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.client.C0FPacketConfirmTransaction

class Vulcan2Velocity : VelocityMode("Vulcan2") {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0FPacketConfirmTransaction && mc.thePlayer.hurtTime > 0) {
            event.cancelEvent()
        }
        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId)
            event.cancelEvent()
    }
}