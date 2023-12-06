package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.value.FloatValue

class StandardVelocity : VelocityMode("Standard") {

    val horizontalValue = FloatValue("Horizontal", 0F, 0F, 100F, "x")
    val verticalValue = FloatValue("Vertical", 0F, 0F, 100F, "x")

	override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            val horizontal = horizontalValue.get()
            val vertical = verticalValue.get()

            packet.motionX = (packet.getMotionX() * horizontal).toInt()
            packet.motionY = (packet.getMotionY() * vertical).toInt()
            packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
        }
	}
}