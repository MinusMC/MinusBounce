package net.minusmc.minusbounce.features.module.modules.movement.speeds.ncp

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.MovementUtils
import net.minecraft.util.MathHelper
import net.minusmc.minusbounce.event.MotionEvent

class NCPYPortSpeed: SpeedMode("NCPYPort", SpeedType.NCP) {
	private var jumps = 0

    override fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.isOnLadder || mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || !MovementUtils.isMoving) return

        if (jumps >= 4 && mc.thePlayer.onGround) jumps = 0

        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = if (jumps <= 1) 0.42 else 0.4
            val f = mc.thePlayer.rotationYaw * 0.017453292f
            mc.thePlayer.motionX -= MathHelper.sin(f) * 0.2
            mc.thePlayer.motionZ += MathHelper.cos(f) * 0.2
            jumps++
        } else if (jumps <= 1) mc.thePlayer.motionY = -5.0

        MovementUtils.strafe()
    }
}
