package net.minusmc.minusbounce.features.module.modules.combat.velocitys.aac

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue

class AACVelocity : VelocityMode("AAC") {
	private val horizontalValue = FloatValue("Horizontal", 0f, 0f, 100f, "%")
    private val verticalValue = FloatValue("Vertical", 0f, 0f, 100f, "%")
	private val aacStrafeValue = BoolValue("StrafeValue", false)
	private var velocityInput = false
	private val velocityTimer = MSTimer()

	override fun onUpdate() {
		if (velocityInput && velocityTimer.hasTimePassed(50)) {
			mc.thePlayer.motionX *= velocity.horizontalValue.get()
			mc.thePlayer.motionZ *= velocity.horizontalValue.get()
			mc.thePlayer.motionY *= velocity.verticalValue.get()
			if(aacStrafeValue.get()) MovementUtils.strafe()
			velocityInput = false
		}
	}

	override fun onPacket(event: PacketEvent) {
		if (event.packet is S12PacketEntityVelocity) velocityInput = true
	}

}