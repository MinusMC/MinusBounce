package net.minusmc.minusbounce.features.module.modules.movement.noslows.ncp

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class NCP2NoSlow : NoSlowMode("NCP2") {
	override fun onMotion(event: MotionEvent) {
		if (event.eventState == EventState.PRE) {
			PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
		} else {
			PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
		}
	}
}