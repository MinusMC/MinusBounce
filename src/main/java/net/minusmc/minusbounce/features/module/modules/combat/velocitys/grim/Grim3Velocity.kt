package net.minusmc.minusbounce.features.module.modules.combat.velocitys.grim

import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class Grim3Velocity : VelocityMode("Grim3") {
    private var grimFlag = false

    override fun onUpdate() {
        if (mc.thePlayer.hurtTime != 0) {
            mc.thePlayer.setPosition(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY, mc.thePlayer.lastTickPosZ)
            grimFlag = true
        }
        if (mc.thePlayer.onGround) grimFlag = false
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
            event.cancelEvent()
            mc.thePlayer.motionX += 0.1
            mc.thePlayer.motionY += 0.1
            mc.thePlayer.motionZ += 0.1
        }
        if (grimFlag && packet is C03PacketPlayer) {
            packet.x = mc.thePlayer.posX + 210.0
            packet.z = mc.thePlayer.posZ + 210.0
        }
    }
}