package net.minusmc.minusbounce.features.module.modules.player.nofalls.other

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.WorldEvent
import net.minusmc.minusbounce.utils.MovementUtils

class VulcanNoFall: NoFallMode("Vulcan") {
    private var doSpoof = false
    private var nextSpoof = false
    private var vulcantNoFall = true
    private var vulcanNoFall = false

    override fun onEnable() {
        nextSpoof = false
        doSpoof = false
    }

    override fun onWorld(event: WorldEvent) {
        vulcantNoFall = true
        vulcanNoFall = false
    }

	override fun onUpdate() {
        if (!vulcanNoFall && mc.thePlayer.fallDistance > 3.25)
            vulcanNoFall = true
        if (vulcanNoFall && vulcantNoFall && mc.thePlayer.onGround)
            vulcantNoFall = false
        if (vulcantNoFall) return // Possible flag
        if (nextSpoof) {
            mc.thePlayer.motionY = -0.1
            mc.thePlayer.fallDistance = -0.1F
            MovementUtils.strafe(0.3F)
            nextSpoof = false
        }
        if (mc.thePlayer.fallDistance > 3.5625F) {
            mc.thePlayer.fallDistance = 0F
            doSpoof = true
            nextSpoof = true
        }
	}

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && doSpoof) {
            packet.onGround = true
            doSpoof = false
            packet.y = Math.round(mc.thePlayer.posY * 2).toDouble() / 2
            mc.thePlayer.setPosition(mc.thePlayer.posX, packet.y, mc.thePlayer.posZ)
        }
    }
}