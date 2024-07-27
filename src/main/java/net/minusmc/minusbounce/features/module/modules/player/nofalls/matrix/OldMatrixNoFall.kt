package net.minusmc.minusbounce.features.module.modules.player.nofalls.matrix

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minusmc.minusbounce.event.*

class OldMatrixNoFall: NoFallMode("OldMatrix") {
	private var isDmgFalling = false
	private var modifiedTimer = false
	private var flagTicks = 0

    override fun onEnable() {
		isDmgFalling = false
	}

	override fun onDisable() {
		isDmgFalling = false
	}

	override fun onUpdate() {
		if (modifiedTimer) {
            mc.timer.timerSpeed = 1.0F
            modifiedTimer = false
        }

        if (flagTicks > 0) {
            flagTicks--
            if (flagTicks == 0)
                mc.timer.timerSpeed = 1F
        }

		if (mc.thePlayer.fallDistance > 3)
			isDmgFalling = true
	}

	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet

		if (isDmgFalling && packet is C03PacketPlayer && packet.onGround && mc.thePlayer.onGround) {
            flagTicks = 2
            isDmgFalling = false
            event.isCancelled = true
            mc.thePlayer.onGround = false
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, packet.y - 256, packet.z, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, -10.0 , packet.z, true))
            mc.timer.timerSpeed = 0.18f
            modifiedTimer = true
        }
	}

	override fun onReceivedPacket(event: ReceivedPacketEvent) {
		val packet = event.packet

		if (packet is S08PacketPlayerPosLook && flagTicks > 0) {
            flagTicks = 0
            mc.timer.timerSpeed = 1.0F
            event.isCancelled = true
        }
	}
}