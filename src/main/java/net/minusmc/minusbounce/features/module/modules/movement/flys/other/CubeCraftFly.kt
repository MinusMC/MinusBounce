package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.misc.MathUtils
import kotlin.math.cos
import kotlin.math.sin

class CubeCraftFly: FlyMode("CubeCraft", FlyType.OTHER) {
	private var ticks = 0

    override fun onUpdate() {
        mc.timer.timerSpeed = 0.6f
        ticks++
    }

    override fun onMove(event: MoveEvent) {
        val yaw = MathUtils.toRadians(mc.thePlayer.rotationYaw)
        
        if (ticks >= 2) {
            event.x = -sin(yaw) * 2.4
            event.z = cos(yaw) * 2.4
            ticks = 0
        } else {
            event.x = -sin(yaw) * 0.2
            event.z = cos(yaw) * 0.2
        }
    }
}