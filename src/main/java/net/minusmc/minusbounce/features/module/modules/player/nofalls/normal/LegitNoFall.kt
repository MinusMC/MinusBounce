package net.minusmc.minusbounce.features.module.modules.player.nofalls.normal

import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB
import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.utils.block.BlockUtils.collideBlock
import net.minusmc.minusbounce.utils.misc.FallingPlayer
import net.minusmc.minusbounce.utils.extensions.*

class LegitNoFall: NoFallMode("Legit") {
    private var working = false
    override fun onUpdate() {

        mc.thePlayer ?: return
        val boundingBox = mc.thePlayer.entityBoundingBox
        val alternativeBoundingBox = AxisAlignedBB(boundingBox.minX, boundingBox.minY - 0.01, 
                boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)

        if (collideBlock(boundingBox, {it is BlockLiquid}) || collideBlock(alternativeBoundingBox, {it is BlockLiquid}))
            return

        if (mc.thePlayer.fallDistance > 3) {
            val fallingPlayer = FallingPlayer(mc.thePlayer)

            if (fallingPlayer.findCollision(1) != null)
                working = true
        }

        if (working && mc.thePlayer.onGround) {
            mc.gameSettings.keyBindSneak.pressed = false
            working = false
        }

        if (working)
            mc.gameSettings.keyBindSneak.pressed = true
    }

    override fun onDisable() {
        mc.gameSettings.keyBindSneak.pressed = false
    }
}