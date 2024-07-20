package net.minusmc.minusbounce.features.module.modules.movement.noslows.sword

import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.PacketUtils


class IntaveHoldSwordNoSlow: NoSlowMode("IntaveHold") {
    override fun onPreMotion(event: PreMotionEvent) {
        if (!MovementUtils.isMoving)
            return

        PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
    }
}