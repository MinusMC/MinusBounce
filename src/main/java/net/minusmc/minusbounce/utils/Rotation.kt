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
import kotlin.math.round

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

    /**
     * Patch gcd exploit in aim
     *
     * @see net.minecraft.client.renderer.EntityRenderer.updateCameraAndRender
     */
    @JvmOverloads
    fun fixedSensitivity(sensitivity: Float, rotation: Rotation? = RotationUtils.serverRotation) {
        rotation?.let{
            val f = sensitivity * (1f + Math.random().toFloat() / 10000000f) * 0.6F + 0.2F
            val m = f * f * f * 8.0F * 0.15f
            yaw = it.yaw + round((yaw - it.yaw) / m) * m
            pitch = (it.pitch + round((pitch - it.pitch) / m) * m).coerceIn(-90F, 90F)
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

    override fun toString(): String {
        return "Rotation(yaw=$yaw, pitch=$pitch)"
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