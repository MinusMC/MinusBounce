package net.minusmc.minusbounce.features.module.modules.movement.noslows.other

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange

class SwitchItemNoSlow : NoSlowMode("SwitchItem") {
	override fun onMotion(event: MotionEvent) {
		PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
		PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
	}
}