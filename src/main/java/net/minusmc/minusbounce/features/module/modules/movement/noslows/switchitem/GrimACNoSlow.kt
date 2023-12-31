package net.minusmc.minusbounce.features.module.modules.movement.noslows.grim

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange

class GrimACNoSlow : NoSlowMode("GrimAC") {
    override fun onPreMotion(event: PreMotionEvent) {
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }
    override fun onPostMotion(event: PostMotionEvent) {
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }
}