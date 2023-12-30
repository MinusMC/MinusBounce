package net.minusmc.minusbounce.features.module.modules.player.antivoids.normal

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.C03PacketPlayer


class GroundSpoofAntiVoid: AntiVoidMode("GroundSpoof") {
    private var canSpoof = false
    override fun onEnable() {
        canSpoof = false
    }

    override fun onUpdate() {
        if (!antivoid.voidOnlyValue.get() || isVoid) {
            canSpoof = mc.thePlayer.fallDistance > antivoid.maxFallDistValue.get()
        }
    }

    override fun onPacket(event: PacketEvent) {
    	val packet = event.packet
    	if (canSpoof && packet is C03PacketPlayer) {
            packet.onGround = true
        }
    }
}