package net.minusmc.minusbounce.features.module.modules.movement.noslows.ncp

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent

class NewNCPNoSlow : NoSlowMode("NewNCP") {
    override fun onPreMotion(event: PreMotionEvent) {
        if (mc.thePlayer.ticksExisted % 2 == 0)
            sendC07(true, 50, true)
    }

    override fun onPostMotion(event: PostMotionEvent) {
        if (mc.thePlayer.ticksExisted % 2 != 0)
            sendC08(false, 0, true, true)
    }
}