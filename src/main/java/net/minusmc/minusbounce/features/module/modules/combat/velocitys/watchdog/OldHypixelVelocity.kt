package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class HypixelVelocity : VelocityMode("Hypixel") {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
              event.cancelEvent()
              mc.thePlayer.motionY = packet.getMotionY().toDouble() / 8000.0
        }
    }
}