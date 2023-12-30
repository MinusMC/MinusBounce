package net.minusmc.minusbounce.features.module.modules.combat.velocitys.minemenclub

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class MineMenClubVelocity : VelocityMode("MinemenClub") {

    private var velocityy = 0

    override fun onPreMotion(event: PreMotionEvent) {
        velocityy++
    }

    override fun onPacket(event: PacketEvent) {
        var packet = event.packet

        if (velocityy > 20) {
            if (packet is S12PacketEntityVelocity) {
                if (packet.entityID == mc.thePlayer.entityId) {
                    event.cancelEvent()
                    velocityy = 0
                }
            } else if (packet is S27PacketExplosion) {
                event.cancelEvent()
                velocityy = 0
            }
        }
    }

}