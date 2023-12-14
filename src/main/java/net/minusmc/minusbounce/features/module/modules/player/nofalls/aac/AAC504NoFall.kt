package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class AAC504NoFall: NoFallMode("AAC 5.0.4") {
    private var isDmgFalling = false

    override fun onEnable() {
        isDmgFalling = false
    }

    override fun onDisable() {
        isDmgFalling = false
    }
    
    override fun onUpdate() {
        if (mc.thePlayer.fallDistance > 3) isDmgFalling = true
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (isDmgFalling && packet is C03PacketPlayer && packet.onGround && mc.thePlayer.onGround) {
            isDmgFalling = false
            packet.onGround = true
            mc.thePlayer.onGround = false
            packet.y += 1.0
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, packet.y - 1.0784, packet.z, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, packet.y - 0.5, packet.z, true))
        }
    }
}