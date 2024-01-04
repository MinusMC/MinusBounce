package net.minusmc.minusbounce.features.module.modules.movement.noslows.other

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minusmc.minusbounce.event.SlowDownEvent

class OldIntaveNoSlow : NoSlowMode("OldIntave") {
    override fun onMotion(event: MotionEvent) {

        if (mc.thePlayer.isUsingItem) {
            PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
            PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }
        PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(mc.thePlayer.inventory.currentItem + 36).stack))
    }
}