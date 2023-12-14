package net.minusmc.minusbounce.features.module.modules.combat.velocitys.aac

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.timer.MSTimer

class AAC4ReduceVelocity : VelocityMode("AAC4Reduce") {
	private var velocityInput = false
	private val velocityTimer = MSTimer()

	override fun onUpdate() {
		if (mc.thePlayer.hurtTime > 0 && !mc.thePlayer.onGround && velocityInput && velocityTimer.hasTimePassed(80L)){
            mc.thePlayer.motionX *= 0.62
            mc.thePlayer.motionZ *= 0.62
        }
        if(velocityInput && (mc.thePlayer.hurtTime<4 || mc.thePlayer.onGround) && velocityTimer.hasTimePassed(120L)) {
            velocityInput = false
        }
	}
	override fun onPacket(event: PacketEvent) {
		val packet = event.packet
		if (packet is S12PacketEntityVelocity) {
			velocityInput = true
	        packet.motionX = (packet.getMotionX() * 0.6).toInt()
	        packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
		}
	}
}