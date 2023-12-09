/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minusmc.minusbounce.event.StrafeEvent
import net.minusmc.minusbounce.utils.block.PlaceInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.client.Rotations
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Rotations
 */
data class Rotation(var yaw: Float, var pitch: Float) {

    /**
     * Set rotations to [player]
     */
    fun toPlayer(player: EntityPlayer) {
        if (yaw.isNaN() || pitch.isNaN())
            return

        fixedSensitivity(MinecraftInstance.mc.gameSettings.mouseSensitivity)

        player.rotationYaw = yaw
        player.rotationPitch = pitch
    }

    /**
     * Patch gcd exploit in aim
     *
     * @see net.minecraft.client.renderer.EntityRenderer.updateCameraAndRender
     */
    fun fixedSensitivity(sensitivity: Float) {
        val sensValue = MinusBounce.moduleManager[Rotations::class.java]!!.rotationSensitivity

        when (sensValue.get().lowercase()) {
            "old" -> {
                val f = sensitivity * 0.6F + 0.2F
                val gcd = f * f * f * 1.2F

                yaw -= yaw % gcd
                pitch -= pitch % gcd
            }
            "new" -> {
                val f = sensitivity * 0.6F + 0.2F
                val gcd = f * f * f * 1.2F

                // get previous rotation
                val rotation = RotationUtils.serverRotation!!

                // fix yaw
                var deltaYaw = yaw - rotation.yaw
                deltaYaw -= deltaYaw % gcd
                yaw = rotation.yaw + deltaYaw

                // fix pitch
                var deltaPitch = pitch - rotation.pitch
                deltaPitch -= deltaPitch % gcd
                pitch = rotation.pitch + deltaPitch
            }
        }
    }

    /**
     * Apply strafe to player
     *
     * @author bestnub
     */
     
    fun toDirection(): Vec3 {
        val f: Float = MathHelper.cos(-yaw * 0.017453292f - Math.PI.toFloat())
        val f1: Float = MathHelper.sin(-yaw * 0.017453292f - Math.PI.toFloat())
        val f2: Float = -MathHelper.cos(-pitch * 0.017453292f)
        val f3: Float = MathHelper.sin(-pitch * 0.017453292f)
        return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }
    
    fun applyStrafeToPlayer(event: StrafeEvent) {
        val player = MinecraftInstance.mc.thePlayer
        val dif = ((MathHelper.wrapAngleTo180_float(player.rotationYaw - this.yaw
                - 23.5f - 135)
                + 180) / 45).toInt()

        val yaw = this.yaw

        val strafe = event.strafe
        val forward = event.forward
        val friction = event.friction

        var calcForward = 0f
        var calcStrafe = 0f

        when (dif) {
            0 -> {
                calcForward = forward
                calcStrafe = strafe
            }
            1 -> {
                calcForward += forward
                calcStrafe -= forward
                calcForward += strafe
                calcStrafe += strafe
            }
            2 -> {
                calcForward = strafe
                calcStrafe = -forward
            }
            3 -> {
                calcForward -= forward
                calcStrafe -= forward
                calcForward += strafe
                calcStrafe -= strafe
            }
            4 -> {
                calcForward = -forward
                calcStrafe = -strafe
            }
            5 -> {
                calcForward -= forward
                calcStrafe += forward
                calcForward -= strafe
                calcStrafe -= strafe
            }
            6 -> {
                calcForward = -strafe
                calcStrafe = forward
            }
            7 -> {
                calcForward += forward
                calcStrafe += forward
                calcForward -= strafe
                calcStrafe += strafe
            }
        }

        if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
            calcForward *= 0.5f
        }

        if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
            calcStrafe *= 0.5f
        }

        var d = calcStrafe * calcStrafe + calcForward * calcForward

        if (d >= 1.0E-4f) {
            d = MathHelper.sqrt_float(d)
            if (d < 1.0f) d = 1.0f
            d = friction / d
            calcStrafe *= d
            calcForward *= d
            val yawSin = MathHelper.sin((yaw * Math.PI / 180f).toFloat())
            val yawCos = MathHelper.cos((yaw * Math.PI / 180f).toFloat())
            player.motionX += calcStrafe * yawCos - calcForward * yawSin.toDouble()
            player.motionZ += calcForward * yawCos + calcStrafe * yawSin.toDouble()
        }
    }
}

/**
 * Rotation with vector
 */
data class VecRotation(val vec: Vec3, val rotation: Rotation)

/**
 * Rotation with place info
 */
data class PlaceRotation(val placeInfo: PlaceInfo, val rotation: Rotation)


// Vestige
data class FixedRotation(var yaw: Float, var pitch: Float, var lastYaw: Float, var lastPitch: Float) {
    constructor(yaw: Float, pitch: Float): this(yaw, pitch, yaw, pitch)

    fun updateRotations(requestedYaw: Float, requestedPitch: Float) {
        lastYaw = yaw
        lastPitch = pitch

        val gcd = ((MinecraftInstance.mc.gameSettings.mouseSensitivity * 0.6 + 0.2).pow(3).toFloat() * 1.2).toFloat()
        val yawDiff = requestedYaw - yaw
        val pitchDiff = requestedPitch - pitch

        val fixedYawDiff = yawDiff - (yawDiff % gcd)
        val fixedPitchDiff = pitchDiff - (pitchDiff % gcd)

        yaw += fixedYawDiff
        pitch += fixedPitchDiff

        pitch = max(-90f, min(90f, pitch))
    }
}
