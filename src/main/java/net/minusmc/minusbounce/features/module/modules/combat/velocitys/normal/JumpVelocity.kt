package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.*

class JumpVelocity : VelocityMode("Jump") {
    override fun onPacket(event: PacketEvent) {
        if (event.packet is S12PacketEntityVelocity && mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42

            val yaw = mc.thePlayer.rotationYaw * 0.017453292F
            mc.thePlayer.motionX -= sin(yaw) * 0.2
            mc.thePlayer.motionZ += cos(yaw) * 0.2
        }
    }
}