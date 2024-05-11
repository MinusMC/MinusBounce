package net.minusmc.minusbounce.features.module.modules.movement.noslows.ncp

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.PreMotionEvent

class OldNCPNoSlow : NoSlowMode("OldNCP") {
    override fun onPreMotion(event: PreMotionEvent) {
        sendC07(false, 0, false)
        sendC08(false, 0, false)
    }
}