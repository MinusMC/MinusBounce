package net.minusmc.minusbounce.features.module.modules.player.nofalls.matrix

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minusmc.minusbounce.event.PacketEvent

class OldMatrixNoFall: NoFallMode("OldMatrix") {
	private var isDmgFalling = false
	private var modifiedTimer = false
	private var matrixFlagWait = 0

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

        if (matrixFlagWait > 0) {
            matrixFlagWait--
            if (matrixFlagWait == 0)
                mc.timer.timerSpeed = 1F
        }

		if (mc.thePlayer.fallDistance > 3) isDmgFalling = true
	}

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet

		if (packet is S08PacketPlayerPosLook && matrixFlagWait > 0) {
            matrixFlagWait = 0
            mc.timer.timerSpeed = 1.0F
            event.isCancelled = true
        }

		if (isDmgFalling && packet is C03PacketPlayer && packet.onGround && mc.thePlayer.onGround) {
            matrixFlagWait = 2
            isDmgFalling = false
            event.isCancelled = true
            mc.thePlayer.onGround = false
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, packet.y - 256, packet.z, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, -10.0 , packet.z, true))
            mc.timer.timerSpeed = 0.18f
            modifiedTimer = true
        }
	}
}