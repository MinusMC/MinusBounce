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
        val f = sensitivity * (1 + Math.random().toFloat() / 10000000) * 0.6F + 0.2F
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
