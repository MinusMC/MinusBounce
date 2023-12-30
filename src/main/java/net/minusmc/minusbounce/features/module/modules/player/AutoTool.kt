/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.features.module.modules.player

import net.minusmc.minusbounce.event.ClickBlockEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import org.lwjgl.input.Mouse

@ModuleInfo(name = "AutoTool", spacedName = "Auto Tool", description = "Automatically selects the best tool in your inventory to mine a block.", category = ModuleCategory.PLAYER)
class AutoTool : Module() {
    private val swapValue = BoolValue("SwapBack", false)
    private var previtem = 0
    private var mining = false
    private var tickDelay = 0
    @EventTarget
    fun onClick(event: ClickBlockEvent) {
        switchSlot(event.clickedBlock ?: return)
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (Mouse.isButtonDown(0)) {
            tickDelay++
        } else tickDelay = 0
        if (!mining && Mouse.isButtonDown(0)) {
            previtem = mc.thePlayer.inventory.currentItem
            mining = true
        }
        if (mining && !Mouse.isButtonDown(0) && swapValue.get()) {
            mc.thePlayer.inventory.currentItem = previtem
            mining = false
        }
    }

    fun switchSlot(blockPos: BlockPos) {
        var bestSpeed = 1
        var bestSlot = 0
        
        val block = mc.theWorld.getBlockState(blockPos).block

        for (i in 0..8) {
            val item = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
            val speed = item.getStrVsBlock(block)

            if (speed > bestSpeed) {
                bestSpeed = speed.toInt()
                bestSlot = i
            }

            if (bestSlot != -1) {
                mc.thePlayer.inventory.currentItem = bestSlot
            }
        }

    }
}