package net.minusmc.minusbounce.features.module.modules.movement.noslows.food

import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.PacketUtils


class IntaveHoldFoodNoSlow: NoSlowMode("IntaveHold") {
    private var usingItem = false

    override fun onDisable() {
        usingItem = false
    }

    override fun onPreMotion(event: PreMotionEvent) {
        if (mc.thePlayer.itemInUseDuration >= 32)
            usingItem = false

        if (!MovementUtils.isMoving)
            return

        if (!usingItem) {
            PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP))
            usingItem = true
        }
    }
}