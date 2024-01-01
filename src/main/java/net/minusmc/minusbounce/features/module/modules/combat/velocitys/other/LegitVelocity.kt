package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class LegitVelocity : VelocityMode("Legit") {
	private var canVelo = false

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId && canVelo) {
            mc.thePlayer.motionX *= 0.6
            mc.thePlayer.motionZ *= 0.6
            mc.thePlayer.isSprinting = false
            mc.thePlayer.jump()
            canVelo = false
        }

        if (packet is S19PacketEntityStatus) {
        	val player = packet.getEntity(mc.theWorld)
            if (player != mc.thePlayer || packet.opCode != 2.toByte()) 
                return
            canVelo = true
        }
    }
}