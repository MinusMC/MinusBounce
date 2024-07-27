package net.minusmc.minusbounce.features.module.modules.combat.velocitys.aac

import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.value.BoolValue

class AAC520Velocity : VelocityMode("AAC5.2.0") {
	private val attackOnlyValue = BoolValue("AttackOnly", true)
	private var canVelocity = false

	override fun onEnable() {
		canVelocity = false
	}

	override fun onAttack(event: AttackEvent) {
		canVelocity = !attackOnlyValue.get() || event.targetEntity != null
	}

	override fun onReceivedPacket(event: ReceivedPacketEvent) {
		val packet = event.packet

		if (packet is S12PacketEntityVelocity) {
			event.isCancelled = true
            if (!mc.isIntegratedServerRunning && canVelocity)
            	mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
		}
	}
}