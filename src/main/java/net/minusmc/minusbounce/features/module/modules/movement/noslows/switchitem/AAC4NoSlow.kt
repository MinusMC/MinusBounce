package net.minusmc.minusbounce.features.module.modules.movement.noslows.other

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class AAC4NoSlow : NoSlowMode("AAC4") {

    override fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST && noslow.isBlocking)
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
    }

    override fun onUpdate() {
        if (noslow.isBlocking) {
            if (mc.thePlayer.ticksExisted % 2 == 0)
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            return
        }
        if (noslow.isEating) {
            val slot = mc.thePlayer.inventory.currentItem
        
            PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(if (slot < 8) slot + 1 else 0))
            PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(slot))
        }
    }
}