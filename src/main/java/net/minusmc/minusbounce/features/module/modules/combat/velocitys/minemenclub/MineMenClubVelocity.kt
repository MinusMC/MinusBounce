package net.minusmc.minusbounce.features.module.modules.combat.velocitys.minemenclub

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class MineMenClubVelocity : VelocityMode("MinemenClub") {

    private var velocityy = 0

    override fun onMotion(event: MotionEvent) {
        this.velocityy++
    }

    override fun onPacket(event: PacketEvent) {
        var packet = event.packet

        if (this.velocityy > 20) {
            if (packet is S12PacketEntityVelocity) {
                val wrapper = packet

                if (wrapper.entityID == mc.thePlayer.entityId) {
                    event.cancelEvent()
                    this.velocityy = 0
                }
            } else if (packet is S27PacketExplosion) {
                event.cancelEvent()
                this.velocityy = 0
            }
        }
    }

}