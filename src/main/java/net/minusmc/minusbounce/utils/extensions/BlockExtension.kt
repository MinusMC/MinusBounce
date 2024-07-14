/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.extensions

import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

interface BlockExtension {
    /**
     * Get vector of block position
     */
    fun BlockPos.getVec() = Vec3(x + 0.5, y + 0.5, z + 0.5)
}


