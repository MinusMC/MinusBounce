package net.minusmc.minusbounce.features.module.modules.combat.velocitys.aac

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.JumpEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity


class AACZeroVelocity : VelocityMode("AACZero") {
	private var velocityInput = false

	override fun onUpdate() {
		if (mc.thePlayer.hurtTime > 0) {
	        if (!velocityInput || mc.thePlayer.onGround || mc.thePlayer.fallDistance > 2F)
	            return

	        mc.thePlayer.addVelocity(0.0, -1.0, 0.0)
	        mc.thePlayer.onGround = true
	    } else
	        velocityInput = false
	}

	override fun onPacket(event: PacketEvent) {
		if (event.packet is S12PacketEntityVelocity) velocityInput = true
	}

	override fun onJump(event: JumpEvent) {
		if (mc.thePlayer.hurtTime > 0) event.cancelEvent()
	}

}