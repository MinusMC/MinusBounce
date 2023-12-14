package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity

class GlitchVelocity : VelocityMode("Glitch") {
    private var velocityInput = false
    override fun onUpdate() {
        mc.thePlayer.noClip = velocityInput
        if (mc.thePlayer.hurtTime == 7)
            mc.thePlayer.motionY = 0.4

        velocityInput = false
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is S12PacketEntityVelocity) {
            if (!mc.thePlayer.onGround) return
            velocityInput = true
            event.cancelEvent()
        }
    }
}