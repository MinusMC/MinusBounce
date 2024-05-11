package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class MMCVelocity : VelocityMode("MMC") {

    private var counter = 0

    override fun onPreMotion(event: PreMotionEvent) {
        counter++
    }

    override fun onPacket(event: PacketEvent) {
        var packet = event.packet

        if (counter > 20) {
            if (packet is S12PacketEntityVelocity) {
                if (packet.entityID == mc.thePlayer.entityId) {
                    event.cancelEvent()
                    counter = 0
                }
            } else if (packet is S27PacketExplosion) {
                event.cancelEvent()
                counter = 0
            }
        }
    }

}