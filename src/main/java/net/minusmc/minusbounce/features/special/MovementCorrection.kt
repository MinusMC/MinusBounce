package net.minusmc.minusbounce.features.special

import net.minusmc.minusbounce.event.*
import net.minecraft.util.MathHelper
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.RotationUtils
import net.minusmc.minusbounce.utils.MovementUtils
import kotlin.math.abs

object MovementCorrection: MinecraftInstance(), Listenable {

	@EventTarget
	fun onMoveInput(event: MoveInputEvent) {
		val rotation = RotationUtils.targetRotation ?: return
        val forward = event.forward
        val strafe = event.strafe
        val yaw = rotation.yaw

        val angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MovementUtils.getRawDirectionRotation(mc.thePlayer.rotationYaw, forward, strafe).toDouble()))

        if (forward == 0f && strafe == 0f)
            return

        var closestForward = 0f
        var closestStrafe = 0f
        var closestDifference = Float.MAX_VALUE

        var predictedForward = -1f
        while (predictedForward <= 1f) {
        	var predictedStrafe = -1f
        	while (predictedStrafe <= 1f) {
        		if (predictedStrafe == 0f && predictedForward == 0f) {
        			predictedStrafe += 1f
        			continue
        		}

        		val predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MovementUtils.getRawDirectionRotation(yaw, predictedForward, predictedStrafe).toDouble()))
                val difference = abs(angle - predictedAngle).toFloat()

                if (difference < closestDifference) {
                    closestDifference = difference
                    closestForward = predictedForward
                    closestStrafe = predictedStrafe
                }
                predictedStrafe += 1f
        	}
        	predictedForward += 1f
        }

        event.forward = closestForward
        event.strafe = closestStrafe
	}


	override fun handleEvents() = true
}