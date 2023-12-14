package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity

class CancelVelocity : VelocityMode("Cancel") {
    override fun onPacket(event: PacketEvent) {
        if (event.packet is S12PacketEntityVelocity) event.cancelEvent()
    }
}