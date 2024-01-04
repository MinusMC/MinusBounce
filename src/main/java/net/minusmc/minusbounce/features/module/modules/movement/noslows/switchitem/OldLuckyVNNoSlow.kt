package net.minusmc.minusbounce.features.module.modules.movement.noslows.other

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.item.ItemFood

class LuckyVNNoSlow : NoSlowMode("LuckyVN") {
    override fun onUpdate() {
        val currentItemStack = mc.thePlayer.inventory.getCurrentItem().item ?: return
        if (currentItemStack is ItemFood && mc.thePlayer.isUsingItem && mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }
    }

    override fun onMotion(event: MotionEvent) {
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem  % 8 + 1))
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }
}