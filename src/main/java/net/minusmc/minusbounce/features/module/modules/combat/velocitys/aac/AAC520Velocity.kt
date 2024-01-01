package net.minusmc.minusbounce.features.module.modules.combat.velocitys.aac

import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.value.BoolValue

class AAC520Velocity : VelocityMode("AAC 5.2.0") {
	private val aac5KillAuraValue = BoolValue("Attack-Only", true)

	override fun onPacket(event: PacketEvent) {
		if (event.packet is S12PacketEntityVelocity) {
			val killAura = MinusBounce.moduleManager[KillAura::class.java]!!
			event.cancelEvent()
            if (!mc.isIntegratedServerRunning && (!aac5KillAuraValue.get() || killAura.target != null)) mc.netHandler.addToSendQueue(
                C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true)
            )
		}
	}
}