package net.minusmc.minusbounce.features.module.modules.movement.noslows.sword

import net.minecraft.network.Packet
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.utils.PacketUtils


class OldIntaveSwordNoSlow : NoSlowMode("OldIntave") {
    override fun onPreMotion(event: PreMotionEvent) {
        PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
    }

    override fun onPostMotion(event: PostMotionEvent) {
        PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
    }

    override fun onUpdate() {
        val currentItem = mc.thePlayer.inventory.currentItem
        val slotIDtoSwitch = if (currentItem == 7) currentItem - 2 else currentItem + 2
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(slotIDtoSwitch))
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(currentItem))
    }
}
