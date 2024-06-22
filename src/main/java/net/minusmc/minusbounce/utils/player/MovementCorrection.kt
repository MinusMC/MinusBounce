/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.player

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.misc.MathUtils

import kotlin.math.*

object MovementCorrection: MinecraftInstance(), Listenable {
    @JvmField
    var type = Type.NONE

    @EventTarget(priority = -2)
    fun onMoveInput(event: MoveInputEvent) {
        when (type) {
            Type.LIQUID_BOUNCE -> liquidBounceMovementCorrection(event)
            Type.RISE -> riseMovementCorrection(event)
        }
    }

    @EventTarget(priority = -2)
    fun onStrafe(event: StrafeEvent){
        if (type == Type.NONE)
            return

        RotationUtils.currentRotation?.let {event.yaw = it.yaw}
    }

    @EventTarget(priority = -2)
    fun onJump(event: JumpEvent){
        if (type == Type.NONE)
            return

        RotationUtils.currentRotation?.let {event.yaw = it.yaw}
    }

    private fun liquidBounceMovementCorrection(event: MoveInputEvent) {
        val forward = event.forward
        val strafe = event.strafe

        val rotation = RotationUtils.currentRotation ?: return
        val offset = MathUtils.toRadians(mc.thePlayer.rotationYaw - rotation.yaw)

        event.forward = round(forward * cos(offset) + strafe * sin(offset))
        event.strafe = round(strafe * cos(offset) - forward * sin(offset))
    }

    private fun riseMovementCorrection(event: MoveInputEvent) {
        val forward = event.forward
        val strafe = event.strafe
        
        if (event.forward == 0f && event.strafe == 0f)
            return

        val rotation = RotationUtils.currentRotation ?: return
        val directionYaw = MovementUtils.getDirection(mc.thePlayer.rotationYaw, strafe, forward)

        val angle = MathUtils.wrapAngleTo180(directionYaw)

        var closestForward = 0
        var closestStrafe = 0
        var closestDifference = Float.MAX_VALUE

        for (predictedForward in -1..1) {
            for (predictedStrafe in -1..1) {
                if (predictedStrafe == 0 && predictedForward == 0) 
                    continue

                val predictedYaw = MovementUtils.getDirection(rotation.yaw, predictedStrafe.toFloat(), predictedForward.toFloat())
                val predictedAngle = MathUtils.wrapAngleTo180(predictedYaw)
                val difference = abs(angle - predictedAngle)

                if (difference < closestDifference) {
                    closestDifference = difference
                    closestForward = predictedForward
                    closestStrafe = predictedStrafe
                }
            }
        }

        event.forward = closestForward.toFloat()
        event.strafe = closestStrafe.toFloat()

    }

    override fun handleEvents() = true

    enum class Type {
        RISE, LIQUID_BOUNCE, NORMAL, NONE
    }
}