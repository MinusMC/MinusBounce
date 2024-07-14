package net.minusmc.minusbounce.features.module.modules.player.nofalls.matrix

import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode

class Matrix62xNoFall: NoFallMode("Matrix6.2.x") {
	private var matrixFalling = false
	private var matrixFallTicks = 0
	private var matrixLastMotionY = 0.0
	private var matrixCanSpoof = false

	override fun onEnable() {
		matrixFalling = false
		matrixFallTicks = 0
		matrixLastMotionY = 0.0
		matrixCanSpoof = false
	}

	override fun onDisable() {
		matrixFalling = false
		matrixFallTicks = 0
		matrixLastMotionY = 0.0
		matrixCanSpoof = false
	}

	override fun onUpdate() {
		if (matrixFalling) {
		    mc.thePlayer.motionX = 0.0
		    mc.thePlayer.motionZ = 0.0
		    mc.thePlayer.jumpMovementFactor = 0f
		    if (mc.thePlayer.onGround) 
		        matrixFalling = false
		}
		if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3F) {
		    matrixFalling = true
		    if (matrixFallTicks == 0) 
		        matrixLastMotionY = mc.thePlayer.motionY
		    mc.thePlayer.motionY = 0.0
		    mc.thePlayer.motionX = 0.0
		    mc.thePlayer.motionZ = 0.0
		    mc.thePlayer.jumpMovementFactor = 0f
		    mc.thePlayer.fallDistance = 3.2f
		    if (matrixFallTicks in 8..9) 
		        matrixCanSpoof = true
		    matrixFallTicks++
		}
		if (matrixFallTicks > 12 && !mc.thePlayer.onGround) {
		    mc.thePlayer.motionY = matrixLastMotionY
		    mc.thePlayer.fallDistance = 0f
		    matrixFallTicks = 0
		    matrixCanSpoof = false
		}
	}

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet

		if (matrixCanSpoof && packet is C03PacketPlayer) {
            packet.onGround = true
            matrixCanSpoof = false
        }
	}
}