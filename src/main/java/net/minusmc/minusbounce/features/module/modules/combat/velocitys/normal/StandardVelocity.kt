package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.value.FloatValue
import org.apache.commons.lang3.RandomUtils

class StandardVelocity : VelocityMode("Standard") {

    override fun onPacket(event: PacketEvent) {
        val p = event.packet
        if (p is S12PacketEntityVelocity) {
            if (RandomUtils.nextInt(1, 100) <= velocity.c.get()) {
                val h = velocity.h.get().toInt()
                val v = velocity.v.get().toInt()

                if (h == 0 && v == 0) {
                    event.cancelEvent()
                }
                p.motionX = (p.getMotionX() * h / 100)
                p.motionY = (p.getMotionY() * v / 100)
                p.motionZ = (p.getMotionZ() * h / 100)
            }
        }
    }
}