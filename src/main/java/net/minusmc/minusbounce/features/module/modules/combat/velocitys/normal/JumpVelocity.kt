package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.utils.misc.MathUtils
import kotlin.math.*

class JumpVelocity : VelocityMode("Jump") {
    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (event.packet is S12PacketEntityVelocity && mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42

            val f = MathUtils.toRadians(mc.thePlayer.rotationYaw)

            mc.thePlayer.motionX -= sin(f) * 0.2
            mc.thePlayer.motionZ += cos(f) * 0.2
        }
    }
}