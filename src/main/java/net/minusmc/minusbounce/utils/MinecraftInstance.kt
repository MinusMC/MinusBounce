/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.injection.implementations.IEntityLivingBase
import net.minusmc.minusbounce.injection.implementations.IEntityPlayerSP

open class MinecraftInstance: BlockExtension {
    companion object {
        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()
    }

    val EntityPlayerSP.rotation: Rotation
        get() = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

    val EntityPlayerSP.prevRotation: Rotation
        get() = Rotation(mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch)

    var EntityLivingBase.realPosX: Double
        get() = (this as IEntityLivingBase).realPosX
        set(value) {
            (this as IEntityLivingBase).realPosX = value
        }

    var EntityLivingBase.realPosY: Double
        get() = (this as IEntityLivingBase).realPosY
        set(value) {
            (this as IEntityLivingBase).realPosY = value
        }

    var EntityLivingBase.realPosZ: Double
        get() = (this as IEntityLivingBase).realPosZ
        set(value) {
            (this as IEntityLivingBase).realPosZ = value
        }

    var EntityPlayerSP.sprintState: Int
        get() = (this as IEntityPlayerSP).sprintState
        set(value) {
            (this as IEntityPlayerSP).sprintState = value
        }
}
