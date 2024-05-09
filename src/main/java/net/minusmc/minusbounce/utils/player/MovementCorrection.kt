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
        val forward = event.forward
        val strafe = event.strafe
        if (type == Type.STRICT && RotationUtils.active) {

            val rotation = RotationUtils.targetRotation ?: return
            val offset = MathUtils.toRadians(mc.thePlayer.rotationYaw - rotation.yaw)

            val calcForward = ceil(abs(forward)) * forward.sign
            val calcStrafe = ceil(abs(strafe)) * strafe.sign

            val f = if (event.forward != 0f) event.forward else event.strafe

            /* Handle NegativeZero */
            event.forward = round(calcForward * cos(offset) + calcStrafe * sin(offset)) * abs(f)
            event.strafe = round(calcStrafe * cos(offset) - calcForward * sin(offset)) * abs(f)
        }
    }

    @EventTarget(priority = -2)
    fun onStrafe(event: StrafeEvent){

        if (type == Type.NONE)
            return

        RotationUtils.targetRotation?.let {if (RotationUtils.active) event.yaw = it.yaw}
    }

    @EventTarget(priority = -2)
    fun onJump(event: JumpEvent){
        if (type == Type.NONE)
            return

        RotationUtils.targetRotation?.let {if (RotationUtils.active) event.yaw = it.yaw}
    }

    override fun handleEvents() = true

    enum class Type {
        STRICT, NORMAL, NONE
    }
}