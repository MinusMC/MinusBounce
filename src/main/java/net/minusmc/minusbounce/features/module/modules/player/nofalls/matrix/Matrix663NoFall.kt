package net.minusmc.minusbounce.features.module.modules.player.nofalls.matrix

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.utils.PacketUtils

class Matrix663NoFall: NoFallMode("Matrix6.6.3") {
	private var sent = false
	private var modifiedTimer = false

	override fun onDisable() {
		sent = false
	}

	override fun onUpdate() {
		if (modifiedTimer) {
            mc.timer.timerSpeed = 1.0F
            modifiedTimer = false
        }

		if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3F) {
            mc.thePlayer.fallDistance = 0.0f
            sent = true
            mc.timer.timerSpeed = 0.5f
            modifiedTimer = true
        }
	}

	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet

		if (sent && packet is C03PacketPlayer) {
            sent = false
            event.isCancelled = true
            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, false))
        }
	}
}