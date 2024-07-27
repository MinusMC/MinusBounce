package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.value.FloatValue

class VerticalVelocity: VelocityMode("Vertical") {
    private val percentageValue = FloatValue("Percentage", 60f, 0f, 100f, "%")

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            packet.motionX = 0
            packet.motionY = (packet.motionY * percentageValue.get() / 100f).toInt()
            packet.motionZ = 0
        }
    }
}