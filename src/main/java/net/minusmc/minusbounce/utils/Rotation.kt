/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.block.BlockPressurePlate.Sensitivity
import net.minusmc.minusbounce.utils.block.PlaceInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import kotlin.math.*

/**
 * Rotations
 */
data class Rotation(var yaw: Float, var pitch: Float) {
    constructor(yaw: Double, pitch: Double): this(yaw.toFloat(), pitch.toFloat())

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

    fun fixedSensitivity(sensitivity: Float) {
        val f = sensitivity * 0.6f + 0.2f
        val gcd = f * f * f * 1.2f

        val serverRotation = RotationUtils.serverRotation

        val deltaYaw = yaw - serverRotation.yaw
        val deltaPitch = pitch - serverRotation.pitch

        val finalYaw = serverRotation.yaw + round(deltaYaw / gcd) * gcd
        val finalPitch = serverRotation.pitch + round(deltaPitch / gcd) * gcd

        yaw = finalYaw
        pitch = finalPitch.coerceIn(-90f, 90f)
    }

    /**
     * Apply strafe to player
     *
     * @author bestnub
     */

    fun toDirection(): Vec3 {
        val yawRad = -MathUtils.toRadians(yaw) - PI
        val pitchRad = -MathUtils.toRadians(pitch) - PI

        val f = cos(yawRad)
        val f1 = sin(yawRad)
        val f2 = -cos(pitchRad)
        val f3 = sin(pitchRad)

        return Vec3(f1 * f2, f3, f * f2)
    }

    override fun toString() = "Rotation(yaw=$yaw, pitch=$pitch)"
}

/**
 * Rotation with vector
 */
data class VecRotation(val vec: Vec3, val rotation: Rotation)

/**
 * Rotation with place info
 */
data class PlaceRotation(val placeInfo: PlaceInfo, val rotation: Rotation)