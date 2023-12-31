package net.minusmc.minusbounce.features.module.modules.movement.noslows.normal

import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.utils.timer.MSTimer

class ExperimentalNoSlow : NoSlowMode("Experimental") {
    private var fasterDelay = false
    private var placeDelay = 0L
    private val timer = MSTimer()

    override fun onPreMotion(event: PreMotionEvent) {
        if ((mc.thePlayer.isUsingItem || mc.thePlayer.isBlocking) && timer.hasTimePassed(placeDelay)) {
            mc.playerController.syncCurrentPlayItem()
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }

    override fun onPostMotion(event: PostMotionEvent) { 
        if ((mc.thePlayer.isUsingItem || mc.thePlayer.isBlocking) && timer.hasTimePassed(placeDelay)) {
            mc.playerController.syncCurrentPlayItem()
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            placeDelay = 200L
            if (fasterDelay) {
                placeDelay = 100L
                fasterDelay = false
            } else
                fasterDelay = true
            timer.reset()
        }
    }
}