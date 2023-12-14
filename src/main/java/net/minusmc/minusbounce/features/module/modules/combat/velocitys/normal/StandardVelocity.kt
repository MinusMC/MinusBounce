package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity

class StandardVelocity : VelocityMode("Standard") {
    val horizontalValue = FloatValue("Horizontal", 0f, 0f, 100f, "%")
    val verticalValue = FloatValue("Vertical", 0f, 0f, 100f, "%")

	override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            val horizontal = velocity.horizontalValue.get()
            val vertical = velocity.verticalValue.get()

            packet.motionX = (packet.getMotionX() * horizontal / 100f).toInt()
            packet.motionY = (packet.getMotionY() * vertical / 100f).toInt()
            packet.motionZ = (packet.getMotionZ() * horizontal / 100f).toInt()
        }
	}
}