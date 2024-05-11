/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.hud.element.elements

import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.Element
import net.minusmc.minusbounce.ui.client.hud.element.ElementInfo
import net.minusmc.minusbounce.ui.client.hud.element.Side
import net.minusmc.minusbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FontValue
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion

/**
 * CustomHUD effects element
 *
 * Shows a list of active potion effects
 */
@ElementInfo(name = "Effects")
class Effects(x: Double = 2.0, y: Double = 10.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    private val anotherStyle = BoolValue("New", false)
    private val fontValue = FontValue("Font", Fonts.font35)
    private val shadow = BoolValue("Shadow", true)

    /**
     * Draw element
     */
    override fun drawElement(): Border {
        val fontRenderer = fontValue.get()

        var y = 0F
        var width = 0F

        assumeNonVolatile = true

        for (effect in mc.thePlayer.activePotionEffects) {
            if (side.vertical == Side.Vertical.DOWN)
                y -= fontRenderer.FONT_HEIGHT + if (anotherStyle.get()) 1F else 0F

            val potion = Potion.potionTypes[effect.potionID]

            val amplifier = (effect.amplifier + 1).coerceIn(1, 11)

            val number = when (amplifier) {
                1 -> "I"
                2 -> "II"
                3 -> "III"
                4 -> "IV"
                5 -> "V"
                6 -> "VI"
                7 -> "VII"
                8 -> "VIII"
                9 -> "IX"
                10 -> "X"
                else -> "X+"
            }

            val duration = if (effect.isPotionDurationMax) 30 else effect.duration / 20
            val name = if (anotherStyle.get())
                "${I18n.format(potion.name)} $number ${if (duration < 15) "§c" else if (duration < 30) "§6" else "§7"}${Potion.getDurationString(effect)}"
            else
                "${I18n.format(potion.name)} $number§f: §7${Potion.getDurationString(effect)}"
            val stringWidth = fontRenderer.getStringWidth(name).toFloat()

            if (side.horizontal == Side.Horizontal.RIGHT) {
                if (width > -stringWidth)
                    width = -stringWidth
            } else {
                if (width < stringWidth)
                    width = stringWidth
            }

            when (side.horizontal) {
                Side.Horizontal.RIGHT -> fontRenderer.drawString(name, -stringWidth, y + if (side.vertical == Side.Vertical.UP) -fontRenderer.FONT_HEIGHT.toFloat() else 0F, potion.liquidColor, shadow.get())
                Side.Horizontal.LEFT, Side.Horizontal.MIDDLE -> fontRenderer.drawString(name, 0F, y + if (side.vertical == Side.Vertical.UP) -fontRenderer.FONT_HEIGHT.toFloat() else 0F, potion.liquidColor, shadow.get())
            }

            if (side.vertical == Side.Vertical.UP)
                y += fontRenderer.FONT_HEIGHT + if (anotherStyle.get()) 1F else 0F
        }

        assumeNonVolatile = false

        if (width == 0F)
            width = if (side.horizontal == Side.Horizontal.RIGHT) -40F else 40F

        if (y == 0F)
            y = if (side.vertical == Side.Vertical.UP) fontRenderer.FONT_HEIGHT.toFloat() else -fontRenderer.FONT_HEIGHT.toFloat()

        return Border(0F, 0F, width, y)
    }
}