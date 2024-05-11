/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.hud.element.elements

import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.Element
import net.minusmc.minusbounce.ui.client.hud.element.ElementInfo
import net.minusmc.minusbounce.ui.client.hud.element.Side
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.ListValue

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */
@ElementInfo(name = "Armor")
class Armor(x: Double = -8.0, y: Double = 57.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)) : Element(x, y, scale, side) {
    private val alignment = ListValue("Alignment", arrayOf("Horizontal", "Vertical"), "Horizontal")

    /**
     * Draw element
     */
    override fun drawElement(): Border {
        if (!mc.thePlayer.capabilities.isCreativeMode) {
            val renderItem = mc.renderItem
            val isInsideWater = mc.thePlayer.isInsideOfMaterial(Material.water)

            var x = 1
            var y = if (isInsideWater) -10 else 0

            RenderHelper.enableGUIStandardItemLighting()
            
            for (index in 3 downTo 0) {
                val stack = mc.thePlayer.inventory.armorInventory[index]
                if (stack != null) {
                    renderItem.renderItemIntoGUI(stack, x, y)
                    renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
                }
                when (alignment.get().lowercase()) {
                    "horizontal" -> x += 21
                    "vertical" -> y += 21
                }
            }
            
            RenderHelper.disableStandardItemLighting()
            GlStateManager.enableAlpha()
            GlStateManager.disableBlend()
            GlStateManager.disableLighting()
            GlStateManager.disableCull()
        }

        return when (alignment.get().lowercase()) {
            "horizontal" -> Border(0F, 0F, 72F, 17F)
            else -> Border(0F, 0F, 18F, 72F)
        }
    }
}
