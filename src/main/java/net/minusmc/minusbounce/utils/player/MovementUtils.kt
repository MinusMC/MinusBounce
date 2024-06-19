/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.player

import net.minecraft.potion.Potion
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.PlayerUtils
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance() {
    val speed: Float
        get() = getSpeed(mc.thePlayer.motionX, mc.thePlayer.motionZ).toFloat()

    val isMoving: Boolean
        get() = mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0f || mc.thePlayer.movementInput.moveStrafe != 0f)

    fun getSpeed(motionX: Double, motionZ: Double) = sqrt(motionX * motionX + motionZ * motionZ)

    fun boost(speed: Float) = boost(speed, mc.thePlayer.rotationYaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing)

    fun boost(speed: Float, yaw: Float, forward: Float, strafe: Float) {
        if (!isMoving) return
        val yaw = getDirectionToRadian(yaw, strafe, forward)
        mc.thePlayer.motionX += -sin(yaw) * speed
        mc.thePlayer.motionZ += cos(yaw) * speed
    }

    fun strafe() = strafe(speed)

    fun strafe(speed: Float) = strafe(speed, mc.thePlayer.rotationYaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing)

    fun strafe(speed: Float, yaw: Float, forward: Float, strafe: Float) {
        if (!isMoving) return
        val yaw = getDirectionToRadian(yaw, forward, strafe)
        mc.thePlayer.motionX = -sin(yaw) * speed
        mc.thePlayer.motionZ = cos(yaw) * speed
    }

    val direction: Float
        get() = getDirection(mc.thePlayer.rotationYaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing)

    val directionToRadian: Double
        get() = MathUtils.toRadians(direction).toDouble()

    fun getDirection(pYaw: Float) = getDirection(pYaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing)

    fun getDirection(pYaw: Float, pForward: Float, pStrafe: Float): Float {
        var rotationYaw = pYaw
        
        if (pForward < 0f) 
            rotationYaw += 180f

        val forward = if (pForward < 0f) -0.5f else if (pForward > 0f) 0.5f else 1f
        val f = if (pStrafe > 0f) -90f else if (pStrafe < 0f) 90f else 0f

        rotationYaw += f * forward
        return rotationYaw
    }

    fun getDirectionToRadian(pYaw: Float) = MathUtils.toRadians(getDirection(pYaw)).toDouble()

    fun getDirectionToRadian(pYaw: Float, pForward: Float, pStrafe: Float) = MathUtils.toRadians(getDirection(pYaw, pForward, pStrafe)).toDouble()

    fun getDistanceMotion(speed: Float, pYaw: Float): DoubleArray {
        val arr = DoubleArray(2)

        val yaw = getDirectionToRadian(pYaw)

        arr[0] = -sin(yaw) * speed
        arr[1] = cos(yaw) * speed
        return arr
    }

    val jumpEffect: Int
        get() = if (mc.thePlayer.isPotionActive(Potion.jump)) mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier + 1 else 0
    
    val speedEffect: Int
        get() = if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1 else 0

    fun getBaseMoveSpeed() = getBaseMoveSpeed(0.2873)

    fun getBaseMoveSpeed(customSpeed: Double): Double {
        var baseSpeed = if (PlayerUtils.isOnIce) 0.258977700006 else customSpeed
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            val amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1)
        }
        return baseSpeed
    }

    fun getJumpBoostModifier(baseJumpHeight: Float) = getJumpBoostModifier(baseJumpHeight, true)

    fun getJumpBoostModifier(baseJumpHeight: Float, potionJump: Boolean): Double {
        var baseJumpHeight = baseJumpHeight
        if (mc.thePlayer.isPotionActive(Potion.jump) && potionJump) {
            val amplifier = mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier
            baseJumpHeight += (amplifier + 1) * 0.1f
        }
        return baseJumpHeight.toDouble()
    }

    fun resetMotion(y: Boolean = false) {
        if (y) mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
    }
}
