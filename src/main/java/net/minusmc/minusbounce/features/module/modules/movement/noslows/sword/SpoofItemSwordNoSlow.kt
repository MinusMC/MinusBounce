package net.minusmc.minusbounce.features.module.modules.movement.noslows.sword

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.item.ItemSword

class SpoofItemSwordNoSlow : NoSlowMode("SpoofItem") {
    override fun onPreMotion(event: PreMotionEvent) {
        val slot = mc.thePlayer.inventory.currentItem

        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(if (slot < 8) slot + 1 else 0))
        PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(slot))
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)
            event.cancelEvent()
    }
}
