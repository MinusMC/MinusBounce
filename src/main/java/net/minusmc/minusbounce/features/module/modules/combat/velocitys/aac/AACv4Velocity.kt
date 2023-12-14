package net.minusmc.minusbounce.features.module.modules.combat.velocitys.aac

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.JumpEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity


class AACv4Velocity : VelocityMode("AACv4") {
	private var velocityInput = false
	private val velocityTimer = MSTimer()

	override fun onUpdate() {
		if (!mc.thePlayer.onGround) {
			if (velocityInput) {
				mc.thePlayer.speedInAir = 0.02f
				mc.thePlayer.motionX *= 0.6
				mc.thePlayer.motionZ *= 0.6
			} else if (velocityTimer.hasTimePassed(80L)) {
				velocityInput = false
				mc.thePlayer.speedInAir = 0.02f
			}
		}
	}


	override fun onPacket(event: PacketEvent) {
		if (event.packet is S12PacketEntityVelocity) {
			velocityTimer.reset()
			velocityInput = true
		}
	}

	override fun onJump(event: JumpEvent) {
		if (mc.thePlayer.hurtTime > 0) event.cancelEvent()
	}
}