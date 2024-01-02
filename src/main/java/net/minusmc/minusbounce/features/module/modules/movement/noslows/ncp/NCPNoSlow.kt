package net.minusmc.minusbounce.features.module.modules.movement.noslows.ncp

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.MotionEvent

class NCPNoSlow : NoSlowMode("NCP") {
    override fun onMotion(event: MotionEvent) {
        sendPacket(event, sendC07 = true, sendC08 = true, delay = false, delayValue = 0, onGround = false)
    }
}