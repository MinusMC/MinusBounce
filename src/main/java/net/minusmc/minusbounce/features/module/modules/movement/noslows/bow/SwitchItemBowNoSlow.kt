package net.minusmc.minusbounce.features.module.modules.movement.noslows.bow

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange

class SwitchItemBowNoSlow : NoSlowMode("SwitchItem") {
	override fun onPreMotion(event: PreMotionEvent) {
		PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
		PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
	}
}