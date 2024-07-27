/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.player

import net.minecraft.potion.Potion
import net.minecraft.util.MovementInput
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.utils.Constants
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.misc.MotionData
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
        get() = mc.thePlayer != null && (mc.thePlayer.moveForward != 0f || mc.thePlayer.moveStrafing != 0f)

    fun getSpeed(motionX: Double, motionZ: Double) = sqrt(motionX * motionX + motionZ * motionZ)

    fun boost(speed: Float) = boost(speed, mc.thePlayer.rotationYaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing)

    fun boost(speed: Float, yaw: Float, forward: Float, strafe: Float) {
        if (!isMoving) return
        val f = getDirectionToRadian(yaw, strafe, forward)
        mc.thePlayer.motionX += -sin(f) * speed
        mc.thePlayer.motionZ += cos(f) * speed
    }

    fun strafe() = strafe(speed)

    fun strafe(speed: Float, strafeUnitAngle: Float = 90f) {
        strafe(speed, mc.thePlayer.rotationYaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing, strafeUnitAngle)
    }

    fun strafe(speed: Float, yaw: Float, strafeUnitAngle: Float = 90f) {
        strafe(speed, yaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing, strafeUnitAngle)
    }

    fun strafe(speed: Float, yaw: Float, forward: Float, strafe: Float, strafeUnitAngle: Float = 90f) {
        if (!isMoving) return
        val f = getDirectionToRadian(yaw, forward, strafe, strafeUnitAngle)
        mc.thePlayer.motionX = -sin(f) * speed
        mc.thePlayer.motionZ = cos(f) * speed
    }

    val direction: Float
        get() = getDirection(mc.thePlayer.rotationYaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing)

    val directionToRadian: Double
        get() = MathUtils.toRadians(direction).toDouble()

    fun getDirection(pYaw: Float, strafeUnitAngle: Float = 90f) = getDirection(pYaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing, strafeUnitAngle)

    fun getDirection(pYaw: Float, pForward: Float, pStrafe: Float, strafeUnitAngle: Float = 90f): Float {
        var rotationYaw = pYaw
        
        if (pForward < 0f) 
            rotationYaw += 180f

        val forward = if (pForward < 0f) -0.5f else if (pForward > 0f) 0.5f else 1f
        val f = if (pStrafe > 0f) -strafeUnitAngle else if (pStrafe < 0f) strafeUnitAngle else 0f

        rotationYaw += f * forward
        return rotationYaw
    }

    fun getDirectionToRadian(pYaw: Float, strafeUnitAngle: Float = 90f) = MathUtils.toRadians(getDirection(pYaw, strafeUnitAngle)).toDouble()

    fun getDirectionToRadian(pYaw: Float, pForward: Float, pStrafe: Float, strafeUnitAngle: Float = 90f) = MathUtils.toRadians(getDirection(pYaw, pForward, pStrafe, strafeUnitAngle)).toDouble()

    fun getDistanceMotion(speed: Float, pYaw: Float): MotionData {
        val yaw = getDirectionToRadian(pYaw)
        return MotionData(-sin(yaw) * speed, cos(yaw) * speed)
    }

    val jumpEffect: Int
        get() = if (mc.thePlayer.isPotionActive(Potion.jump)) mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier + 1 else 0
    
    val speedEffect: Int
        get() = if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1 else 0


    val baseMoveSpeed: Double
        get() {
            var baseSpeed = if (PlayerUtils.isOnIce) 0.258977700006 else 0.2873
            baseSpeed *= 1.0 + 0.2 * speedEffect
            return baseSpeed 
        }

    fun getJumpBoostModifier(baseJumpHeight: Float) = getJumpBoostModifier(baseJumpHeight, true)

    fun getJumpBoostModifier(baseJumpHeight: Float, potionJump: Boolean) = baseJumpHeight + jumpEffect * 0.1f

    fun resetMotion(y: Boolean = false) {
        if (y) mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
    }

    /**
     * Gets the players' movement yaw
     */

    fun useDiagonalSpeed() {
        val increase = if (mc.thePlayer.onGround) Constants.GROUND_ACCELERATION else Constants.AIR_ACCELERATION
        val downedKeysCount = Constants.moveKeys.count {it.isKeyDown}

        if (mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing != 0f && downedKeysCount == 1) {
            val f = getDirectionToRadian(mc.thePlayer.rotationYaw, strafeUnitAngle = 70f)
            mc.thePlayer.motionX = -sin(f) * increase
            mc.thePlayer.motionZ = cos(f) * increase
        }
    }
}
