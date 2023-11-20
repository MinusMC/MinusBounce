package net.minusmc.minusbounce.features.module.modules.movement.longjumps.watchdog

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode

class WatchdogLongJump : LongJumpMode("Watchdog") {

    private var started = false

    private var counter = 0

    private var ticks = 0

    private val velocityX = 0.0

    private var velocityY = 0.0

    private val velocityZ = 0.0

    private var oldX = 0.0

    private var oldZ = 0.0
    override fun onEnable() {
        ticks = 0
        counter = ticks
        started = false
        velocityY = -1.0
        oldX = mc.thePlayer.posX
        oldZ = mc.thePlayer.posZ
        if (!mc.thePlayer.onGround) counter = 3
    }

}
