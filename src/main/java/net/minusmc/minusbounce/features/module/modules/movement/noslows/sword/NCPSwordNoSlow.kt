package net.minusmc.minusbounce.features.module.modules.movement.noslows.sword

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.PacketUtils

import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class NCPSwordNoSlow : NoSlowMode("NCP") {

	override fun onPreMotion(event: PreMotionEvent) {
		PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
	}

	override fun onPostMotion(event: PostMotionEvent) {
		val heldItem = mc.thePlayer.heldItem ?: return
		PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f))
	}

}