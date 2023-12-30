package net.minusmc.minusbounce.ui.client.clickgui.styles.other.liquidbounceplus.element.components

import net.minusmc.minusbounce.ui.client.clickgui.styles.other.liquidbounceplus.ColorManager
import net.minusmc.minusbounce.ui.client.clickgui.styles.other.liquidbounceplus.extensions.animSmooth
import net.minusmc.minusbounce.utils.render.ShaderUtils
import java.awt.Color

class RangeSlider {
    private var minSmooth = 0F
    private var maxSmooth = 0F
    private var minValue = 0F
    private var maxValue = 0F

    fun onDraw(x: Float, y: Float, width: Float, accentColor: Color) {
        minSmooth = minSmooth.animSmooth(minValue, 0.5F)
        maxSmooth = maxSmooth.animSmooth(maxValue, 0.5F)
        ShaderUtils.drawRoundedRect(x - 1F, y - 1F, x + width + 1F, y + 1F, 1F, ColorManager.unusedSlider)
        //ShaderUtils.drawRoundedRect(x - 1F, y - 1F, x + width * (minSmooth / 100F) + 1F, y + 1F, 1F, accentColor)
        ShaderUtils.drawFilledCircle(x + width * (minSmooth / 100F), y, 5F, Color.white)
        ShaderUtils.drawFilledCircle(x + width * (minSmooth / 100F), y, 3F, ColorManager.background)

        // max value
        ShaderUtils.drawFilledCircle(x + width * (maxValue / 100F), y, 5F, Color.white)
        ShaderUtils.drawFilledCircle(x + width * (maxValue / 100F), y, 3F, ColorManager.background)
    }

    fun setMinValue(desired: Float, min: Float, max: Float) {
        minValue = (desired - min) / (max - min) * 100F
    }

    fun setMaxValue(desired: Float, min: Float, max: Float) {
        maxValue = (desired - min) / (max - min) * 100F
    }
}
