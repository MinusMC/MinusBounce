/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP

open class MinecraftInstance {
    companion object {
        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()
    }

    val EntityPlayerSP.rotation: Rotation
        get() = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
}
