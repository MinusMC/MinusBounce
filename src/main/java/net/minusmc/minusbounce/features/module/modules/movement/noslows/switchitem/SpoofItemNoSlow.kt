package net.minusmc.minusbounce.features.module.modules.movement.noslows.other

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.item.ItemSword

class SpoofItemNoSlow : NoSlowMode("SpoofItem") {
    override fun onMotion(event: MotionEvent) {
        val slot = mc.thePlayer.inventory.currentItem

        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(if (slot < 8) slot + 1 else 0))
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(slot))
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM && mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword) {
            event.cancelEvent()
            val slot = mc.thePlayer.inventory.currentItem

            mc.netHandler.networkManager.sendPacket(C09PacketHeldItemChange(if (slot < 8) slot + 1 else 0))
            mc.netHandler.networkManager.sendPacket(C09PacketHeldItemChange(slot))
        }
    }
}
