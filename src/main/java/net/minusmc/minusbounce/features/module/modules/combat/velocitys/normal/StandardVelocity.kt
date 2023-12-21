package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.value.FloatValue
import net.minecraft.network.play.server.S12PacketEntityVelocity

class StandardVelocity : VelocityMode("Standard") {
    private val horizontalValue = FloatValue("Horizontal", 0f, 0f, 100f, "%")
    private val verticalValue = FloatValue("Vertical", 0f, 0f, 100f, "%")

	override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            val horizontal = horizontalValue.get()
            val vertical = verticalValue.get()

            packet.motionX = (packet.motionX * horizontal / 100f).toInt()
            packet.motionY = (packet.motionY * vertical / 100f).toInt()
            packet.motionZ = (packet.motionZ * horizontal / 100f).toInt()
        }
	}
}