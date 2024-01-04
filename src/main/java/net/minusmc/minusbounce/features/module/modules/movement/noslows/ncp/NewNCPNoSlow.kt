package net.minusmc.minusbounce.features.module.modules.movement.noslows.ncp

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.MotionEvent

class NewNCPNoSlow : NoSlowMode("NewNCP") {
    override fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.ticksExisted % 2 == 0)
            sendPacket(event, sendC07 = true, sendC08 = false, delay = false, delayValue = 50, onGround = true)
        else
            sendPacket(
                event,
                sendC07 = false,
                sendC08 = true,
                delay = false,
                delayValue = 0,
                onGround = true,
                watchDog = true
            )
    }
}