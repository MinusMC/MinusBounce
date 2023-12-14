package net.minusmc.minusbounce.features.module.modules.combat.velocitys.aac

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.timer.MSTimer

class AAC5ReduceVelocity : VelocityMode("AAC5Reduce") {
	private var velocityInput = false
	private val velocityTimer = MSTimer()

	override fun onUpdate() {
		if (mc.thePlayer.hurtTime > 1 && velocityInput) {
            mc.thePlayer.motionX *= 0.81
            mc.thePlayer.motionZ *= 0.81
        }
        if(velocityInput && (mc.thePlayer.hurtTime<5 || mc.thePlayer.onGround) && velocityTimer.hasTimePassed(120L)) {
            velocityInput = false
        }
	}
	override fun onPacket(event: PacketEvent) {
		if (event.packet is S12PacketEntityVelocity) velocityInput = true
	}
}