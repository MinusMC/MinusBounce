package net.minusmc.minusbounce.features.module.modules.movement.noslows.other

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class SwitchItemPreNoSlow: NoSlowMode("SwitchItemPre") {
    override fun onPreMotion(event: PreMotionEvent) {
        mc.playerController.syncCurrentPlayItem()
        PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
    }
    override fun onPostMotion(event: PostMotionEvent) {
        mc.playerController.syncCurrentPlayItem()
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }
}
