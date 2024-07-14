package net.minusmc.minusbounce.features.module.modules.player.nofalls.matrix

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.utils.PacketUtils

class Matrix663NoFall: NoFallMode("Matrix6.6.3") {
	private var matrixSend = false
	private var modifiedTimer = false

	override fun onDisable() {
		matrixSend = false
	}

	override fun onUpdate() {
		if (modifiedTimer) {
            mc.timer.timerSpeed = 1.0F
            modifiedTimer = false
        }

		if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3F) {
            mc.thePlayer.fallDistance = 0.0f
            matrixSend = true
            mc.timer.timerSpeed = 0.5f
            modifiedTimer = true
        }
	}

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet

		if (matrixSend && packet is C03PacketPlayer) {
            matrixSend = false
            event.isCancelled = true
            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, false))
        }
	}
}