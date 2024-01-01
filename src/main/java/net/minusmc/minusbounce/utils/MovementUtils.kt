/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.block.BlockIce
import net.minecraft.block.BlockPackedIce
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.features.module.modules.movement.TargetStrafe
import net.minecraft.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance() {
    private var lastX = -999999.0
    private var lastZ = -999999.0

    val speed: Float
        get() = getSpeed(mc.thePlayer.motionX, mc.thePlayer.motionZ).toFloat()

    val isMoving: Boolean
        get() = mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0f || mc.thePlayer.movementInput.moveStrafe != 0f)

    fun getSpeed(motionX: Double, motionZ: Double): Double {
        return sqrt(motionX * motionX + motionZ * motionZ)
    }

    fun accelerate() {
        accelerate(speed)
    }

    fun accelerate(speed: Float) {
        if (!isMoving) return
        val yaw = direction
        mc.thePlayer.motionX += -sin(yaw) * speed
        mc.thePlayer.motionZ += cos(yaw) * speed
    }

    fun hasMotion(): Boolean {
        return mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0 && mc.thePlayer.motionY != 0.0
    }

    fun strafe() {
        strafe(speed)
    }

    fun strafe(speed: Float) {
        if (!isMoving) return
        val yaw = direction
        mc.thePlayer.motionX = -sin(yaw) * speed
        mc.thePlayer.motionZ = cos(yaw) * speed
    }

    fun strafe(speed: Float, yaw: Float, forward: Float, strafe: Float) {
        if (!isMoving) return
        val yaw = getDirectionRotation(yaw, strafe, forward)
        mc.thePlayer.motionX = -sin(yaw) * speed
        mc.thePlayer.motionZ = cos(yaw) * speed
    }

    fun forward(length: Double) {
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        mc.thePlayer.setPosition(
            mc.thePlayer.posX + -sin(yaw) * length, mc.thePlayer.posY,
            mc.thePlayer.posZ + cos(yaw) * length
        )
    }

    fun getDirection() {
        val ts = MinusBounce.moduleManager[TargetStrafe::class.java] ?: return getRawDirection()
        return if (ts != null && ts!!.canStrafe) 
            ts.getMovingDir()
        else
            getRawDirection()
    }

    fun getRawDirection() {
        return getRawDirection(mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)
    }

    fun getRawDirection(yaw: Float): Float {
        return getRawDirection(yaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)
    }

    fun getRawDirection(yaw: Float, strafe: Float, forward: Float): Float {
        var rotationYaw = yaw
        if (forward < 0f) rotationYaw += 180f
        var forward = 1f
        if (forward < 0f) forward = -0.5f else if (forward > 0f) forward = 0.5f
        if (strafe > 0f) rotationYaw -= 90f * forward
        if (strafe < 0f) rotationYaw += 90f * forward
        return rotationYaw
    }

    fun getDirectionRotation(yaw: Float, strafe: Float, forward: Float): Double {
        return Math.toRadians(getRawDirection(yaw, strafe, forward))
    }

    fun getXZDist(speed: Float, cYaw: Float): DoubleArray {
        val arr = DoubleArray(2)
        val yaw = getDirectionRotation(cYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)
        arr[0] = -sin(yaw) * speed
        arr[1] = cos(yaw) * speed
        return arr
    }

    fun getPredictionYaw(x: Double, z: Double): Float {
        if (mc.thePlayer == null) {
            lastX = -999999.0
            lastZ = -999999.0
            return 0f
        }
        if (lastX == -999999.0) lastX = mc.thePlayer.prevPosX
        if (lastZ == -999999.0) lastZ = mc.thePlayer.prevPosZ
        val returnValue = (atan2(z - lastZ, x - lastX) * 180f / Math.PI).toFloat()
        lastX = x
        lastZ = z
        return returnValue
    }

    val jumpEffect: Int
        get() = if (mc.thePlayer.isPotionActive(Potion.jump)) mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier + 1 else 0
    
    val speedEffect: Int
        get() = if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1 else 0

    fun getBaseMoveSpeed() {
        return getBaseMoveSpeed(0.2873)
    }

    fun getBaseMoveSpeed(customSpeed: Double): Double {
        var baseSpeed = if (isOnIce) 0.258977700006 else customSpeed
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            val amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1)
        }
        return baseSpeed
    }

    fun getJumpBoostModifier(baseJumpHeight: Float): Double {
        return getJumpBoostModifier(baseJumpHeight, true)
    }

    fun getJumpBoostModifier(baseJumpHeight: Float, potionJump: Boolean): Double {
        var baseJumpHeight = baseJumpHeight
        if (mc.thePlayer.isPotionActive(Potion.jump) && potionJump) {
            val amplifier = mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier
            baseJumpHeight += (amplifier + 1) * 0.1f
        }
        return baseJumpHeight.toDouble()
    }

    fun setMotion(event: MoveEvent, speed: Double, motion: Double, smoothStrafe: Boolean) {
        var forward = mc.thePlayer.movementInput.moveForward.toDouble()
        var strafe = mc.thePlayer.movementInput.moveStrafe.toDouble()
        var yaw = mc.thePlayer.rotationYaw.toDouble()
        val direction = if (smoothStrafe) 45 else 90
        if (forward == 0.0 && strafe == 0.0) {
            event.x = 0.0
            event.z = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -direction else direction).toDouble()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) direction else -direction).toDouble()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            val cos = cos(Math.toRadians(yaw + 90.0f))
            val sin = sin(Math.toRadians(yaw + 90.0f))
            event.x = (forward * speed * cos + strafe * speed * sin) * motion
            event.z = (forward * speed * sin - strafe * speed * cos) * motion
        }
    }

    fun setMotion(speed: Double, smoothStrafe: Boolean) {
        var forward = mc.thePlayer.movementInput.moveForward.toDouble()
        var strafe = mc.thePlayer.movementInput.moveStrafe.toDouble()
        var yaw = mc.thePlayer.rotationYaw
        val direction = if (smoothStrafe) 45 else 90
        if (forward == 0.0 && strafe == 0.0) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -direction else direction).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) direction else -direction).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            val cos = cos(Math.toRadians(yaw + 90.0f))
            val sin = sin(Math.toRadians(yaw + 90.0f))
            mc.thePlayer.motionX = forward * speed * -sin + strafe * speed * cos
            mc.thePlayer.motionZ = forward * speed * cos - strafe * speed * -sin
        }
    }

    fun setSpeed(event: MoveEvent, speed: Double) {
        setSpeed(event, speed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe.toDouble(), mc.thePlayer.movementInput.moveForward.toDouble())
    }

    fun setSpeed(event: MoveEvent, speed: Double, yaw: Float, forward: Double, strafe: Double) {
        var forward = strafe
        var strafe = forward
        var yaw = yaw
        if (forward == 0.0 && strafe == 0.0) {
            event.z = 0.0
            event.x = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            if (strafe > 0.0) {
                strafe = 1.0
            } else if (strafe < 0.0) {
                strafe = -1.0
            }
            val cos = cos(Math.toRadians((yaw + 90.0f).toDouble()))
            val sin = sin(Math.toRadians((yaw + 90.0f).toDouble()))
            moveEvent.x = forward * speed * cos + strafe * speed * sin
            moveEvent.z = forward * speed * sin - strafe * speed * cos
        }
    }

    fun incrementMoveDirection(forward: Float, strafe: Float): FloatArray {
        if (forward != 0f || strafe != 0f) {
            val value = if (forward != 0f) Math.abs(forward) else Math.abs(strafe)
            if (forward > 0) {
                if (strafe > 0) {
                    return floatArrayOf(0f, 0f)
                } else if (strafe == 0f) {
                    return floatArrayOf(-value, 0f)
                } else if (strafe < 0) {
                    return floatArrayOf(0f, 0f)
                }
            } else if (forward == 0f) {
                if (strafe > 0) {
                    return floatArrayOf(value, 0f)
                } else {
                    return floatArrayOf(-value, 0f)
                }
            } else {
                if (strafe < 0) {
                    return floatArrayOf(0f, 0f)
                } else if (strafe == 0f) {
                    return floatArrayOf(0f, value)
                } else if (strafe > 0) {
                    return floatArrayOf(0f, 0f)
                }
            }
        }
        return floatArrayOf(forward, strafe)
    }


    fun resetMotion(y: Boolean = false) {
        if (y) mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
    }

    val movingYaw: Float
        get() = (direction * 180f / Math.PI).toFloat()

    fun setMotion2(multiplier: Double, forward: Float) {
        mc.thePlayer.motionX = -Math.sin(Math.toRadians(forward.toDouble())) * multiplier
        mc.thePlayer.motionZ = Math.cos(Math.toRadians(forward.toDouble())) * multiplier
    }

    fun lbFixMove(event: MoveInputEvent): MoveInputEvent {
        if (targetRotation == null) return

        val forward = event.forward
        val strafe = event.strafe

        val rotationOffset = Math.toRadians(mc.thePlayer.rotationYaw - targetRotation.getYaw()).toFloat()
        val cosValue = cos(rotationOffset)
        val sinValue = sin(rotationOffset)

        event.forward = round(forward * cosValue + strafe * sinValue)
        event.strafe = round(strafe * cosValue - forward * sinValue)
        return event
    }
}
