package net.minusmc.minusbounce.features.module.modules.player.nofalls.matrix

import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode

class Matrix62xNoFall: NoFallMode("Matrix6.2.x") {
	private var falling = false
	private var ticks = 0
	private var lastMotionY = 0.0
	private var canSpoof = false

	override fun onEnable() {
		falling = false
		ticks = 0
		lastMotionY = 0.0
		canSpoof = false
	}

	override fun onDisable() {
		falling = false
		ticks = 0
		lastMotionY = 0.0
		canSpoof = false
	}

	override fun onUpdate() {
		if (falling) {
		    mc.thePlayer.motionX = 0.0
		    mc.thePlayer.motionZ = 0.0
		    mc.thePlayer.jumpMovementFactor = 0f
		    if (mc.thePlayer.onGround) 
		        falling = false
		}

		if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3F) {
		    falling = true
		    if (ticks == 0) 
		        lastMotionY = mc.thePlayer.motionY
		    mc.thePlayer.motionY = 0.0
		    mc.thePlayer.motionX = 0.0
		    mc.thePlayer.motionZ = 0.0
		    mc.thePlayer.jumpMovementFactor = 0f
		    mc.thePlayer.fallDistance = 3.2f
		    if (ticks in 8..9) 
		        canSpoof = true
		    ticks++
		}
		
		if (ticks > 12 && !mc.thePlayer.onGround) {
		    mc.thePlayer.motionY = lastMotionY
		    mc.thePlayer.fallDistance = 0f
		    ticks = 0
		    canSpoof = false
		}
	}

	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet

		if (canSpoof && packet is C03PacketPlayer) {
            packet.onGround = true
            canSpoof = false
        }
	}
}