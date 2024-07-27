package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.utils.player.MovementUtils

class MinemenVelocity : VelocityMode("Minemen") {

    private var ticks = 0
    private var lastCancel = false
    private var canCancel = false

    override fun onUpdate() {
        ticks++
        if (ticks > 23)
            canCancel = true

        if (ticks in 2..4 && !lastCancel) {
            mc.thePlayer.motionX *= 0.99
            mc.thePlayer.motionZ *= 0.99
        } else if (ticks == 5 && !lastCancel)
            MovementUtils.strafe()
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
            ticks = 0
            if (canCancel) {
                event.isCancelled = true
                lastCancel = true
                canCancel = false
            } else {
                mc.thePlayer.jump()
                lastCancel = false
            }
        }
    }
}
