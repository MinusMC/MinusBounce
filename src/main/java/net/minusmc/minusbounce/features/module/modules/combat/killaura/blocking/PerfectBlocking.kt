package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking
import net.minecraft.util.*
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*

class PerfectBlocking: KillAuraBlocking("Perfect") {

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet
		if (blockingStatus && ((packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) || packet is C08PacketPlayerBlockPlacement)) event.cancelEvent()
		if (packet is C09PacketHeldItemChange) blockingStatus = false
	}

	override fun onPostMotion() {
		PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
		blockingStatus = true
	}

	override fun onPreAttack() {
		blockingStatus = false
	}
}
