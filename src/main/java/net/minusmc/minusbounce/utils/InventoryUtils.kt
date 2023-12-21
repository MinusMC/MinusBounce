/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.block.Block
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minusmc.minusbounce.event.ClickWindowEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Listenable
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.utils.timer.MSTimer
import java.util.*

class InventoryUtils : MinecraftInstance(), Listenable {
    @EventTarget
    fun onClick(event: ClickWindowEvent?) {
        CLICK_TIMER.reset()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C08PacketPlayerBlockPlacement) CLICK_TIMER.reset()
    }

    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        val CLICK_TIMER = MSTimer()
        val BLOCK_BLACKLIST = Arrays.asList(
            Blocks.enchanting_table,
            Blocks.chest,
            Blocks.ender_chest,
            Blocks.trapped_chest,
            Blocks.anvil,
            Blocks.sand,
            Blocks.web,
            Blocks.torch,
            Blocks.crafting_table,
            Blocks.furnace,
            Blocks.waterlily,
            Blocks.dispenser,
            Blocks.stone_pressure_plate,
            Blocks.wooden_pressure_plate,
            Blocks.noteblock,
            Blocks.dropper,
            Blocks.tnt,
            Blocks.standing_banner,
            Blocks.wall_banner,
            Blocks.redstone_torch,  // recently added
            Blocks.gravel,
            Blocks.cactus,
            Blocks.bed,
            Blocks.lever,
            Blocks.standing_sign,
            Blocks.wall_sign,
            Blocks.jukebox,
            Blocks.oak_fence,
            Blocks.spruce_fence,
            Blocks.birch_fence,
            Blocks.jungle_fence,
            Blocks.dark_oak_fence,
            Blocks.oak_fence_gate,
            Blocks.spruce_fence_gate,
            Blocks.birch_fence_gate,
            Blocks.jungle_fence_gate,
            Blocks.dark_oak_fence_gate,
            Blocks.nether_brick_fence,  //Blocks.cake,
            Blocks.trapdoor,
            Blocks.melon_block,
            Blocks.brewing_stand,
            Blocks.cauldron,
            Blocks.skull,
            Blocks.hopper,
            Blocks.carpet,
            Blocks.redstone_wire,
            Blocks.light_weighted_pressure_plate,
            Blocks.heavy_weighted_pressure_plate,
            Blocks.daylight_detector
        )

        fun findItem(startSlot: Int, endSlot: Int, item: Item): Int {
            for (i in startSlot until endSlot) {
                val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (stack != null && stack.item === item) return i
            }
            return -1
        }

        fun hasSpaceHotbar(): Boolean {
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: return true
            }
            return false
        }

        fun canPlaceBlock(block: Block): Boolean {
            return block.isFullCube && !BLOCK_BLACKLIST.contains(block)
        }

        fun findAutoBlockBlock(): Int {
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock && itemStack.stackSize > 0) {
                    val itemBlock = itemStack.item as ItemBlock
                    val block = itemBlock.getBlock()
                    if (block.isFullCube && !BLOCK_BLACKLIST.contains(block)) return i
                }
            }
            return -1
        }
    }
}
