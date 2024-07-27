package net.minusmc.minusbounce.features.module.modules.movement.longjumps.redesky

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import kotlin.math.min
import kotlin.math.max

class InfiniteRedeskyLongJump : LongJumpMode("InfiniteRedesky") {
	override fun onUpdate() {
		if (mc.thePlayer.fallDistance > 0.6f) 
            mc.thePlayer.motionY += 0.02
    
        val speed = MovementUtils.speed * 1.05878f

        MovementUtils.strafe(speed.coerceIn(0.25f, 0.85f))
	}
}
