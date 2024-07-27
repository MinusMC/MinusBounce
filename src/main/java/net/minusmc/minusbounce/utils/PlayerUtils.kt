package net.minusmc.minusbounce.utils

import net.minecraft.block.BlockIce
import net.minecraft.block.BlockPackedIce
import net.minecraft.block.BlockSlime
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemEnderPearl
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import net.minecraft.util.AxisAlignedBB
import net.minusmc.minusbounce.utils.extensions.*

/**
 * Utils for player
 */

object PlayerUtils: MinecraftInstance() {

	fun getSlimeSlot(): Int {
        for(i in 36..44) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (stack != null && stack.item != null) {
            	if (stack.item is ItemBlock) {
            		val item = stack.item as ItemBlock
	            	if (item.getBlock() is BlockSlime) return i - 36
            	}
            }
        }
        return -1
    }

    fun getPearlSlot(): Int {
        for(i in 36..44) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (stack != null && stack.item is ItemEnderPearl) return i - 36
        }
        return -1
    }

    fun isHealPotion(stack: ItemStack): Boolean {
        val itempotion = ItemPotion()
        val effects = itempotion.getEffects(stack)
        for (effect in effects) {
            if (effect.effectName == "potion.heal") return true
        }
        return false
    }

    fun getHealPotion(): Int {
        for (i in 36..44) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (stack != null && stack.item is ItemPotion && isHealPotion(stack))
                return i - 36
        }
        return -1
    }

    val isOnEdge: Boolean
        get() = mc.thePlayer.onGround && !mc.thePlayer.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown && !mc.gameSettings.keyBindJump.isKeyDown && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001)).isEmpty()

    val isOnIce: Boolean
        get() = mc.theWorld.getBlockState(BlockPos(mc.thePlayer).down()).block.let {it is BlockIce || it is BlockPackedIce}

    val isBlockUnder: Boolean
        get() = isInAir(mc.thePlayer.posY + 2.0, 2.0)

    fun isInAir(height: Double, plus: Double): Boolean {
        if (mc.thePlayer == null || mc.thePlayer.posY < 0.0) 
            return false

        for (off in 0.0..height step plus) {
            val boundingBox = AxisAlignedBB(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.posX, mc.thePlayer.posY - off, mc.thePlayer.posZ)
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, boundingBox).isNotEmpty())
                return true
        }

        return false
    }
}