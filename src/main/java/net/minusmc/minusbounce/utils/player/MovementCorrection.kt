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
    fun onMoveInput(event: MoveInputEvent){
        val forward = event.forward
        val strafe = event.strafe
        if (type == Type.STRICT && RotationUtils.active) {

            val rotation = RotationUtils.targetRotation ?: return
            val offset = MathUtils.toRadians(mc.thePlayer.rotationYaw - rotation.yaw)

            /* Handle NegativeZero */
            event.forward = round(forward * cos(offset) + strafe * sin(offset)) + 0.0f
            event.strafe = round(strafe * cos(offset) - forward * sin(offset)) + 0.0f
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