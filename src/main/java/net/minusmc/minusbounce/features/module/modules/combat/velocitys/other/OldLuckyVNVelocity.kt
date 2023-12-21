package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity

class OldLuckyVNVelocity : VelocityMode("OldLuckyVN") {

	override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            packet.motionX = 0
            packet.motionZ = 0
        }
	}
}