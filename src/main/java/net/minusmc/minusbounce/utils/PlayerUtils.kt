package net.minusmc.minusbounce.utils

import net.minecraft.block.BlockSlime
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemEnderPearl
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemStack
import net.minusmc.minusbounce.utils.MinecraftInstance.Companion.mc

object PlayerUtils {
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
            if(stack != null && stack.item is ItemPotion && isHealPotion(stack)) return i - 36
        }
        return -1
    }

    fun isOnEdge(): Boolean {
        return mc.thePlayer.onGround && !mc.thePlayer.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown && !mc.gameSettings.keyBindJump.isKeyDown && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001)).isEmpty()
    }
}