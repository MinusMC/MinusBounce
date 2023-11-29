package net.minusmc.minusbounce.ui.client.clickgui.styles.other.liquidbounceplus.element.module.value.impl

import net.minusmc.minusbounce.ui.client.clickgui.styles.other.liquidbounceplus.element.components.Checkbox
import net.minusmc.minusbounce.ui.client.clickgui.styles.other.liquidbounceplus.element.module.value.ValueElement
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.MouseUtils
import net.minusmc.minusbounce.value.BoolValue

import java.awt.Color

class BooleanElement(value: BoolValue): ValueElement<Boolean>(value) {
    private val checkbox = Checkbox()

    override fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, bgColor: Color, accentColor: Color): Float {
        checkbox.state = value.get()
        checkbox.onDraw(x + 10F, y + 5F, 10F, 10F, bgColor, accentColor)
        Fonts.font40.drawString(value.name, x + 25F, y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)
        return valueHeight
    }

    override fun onClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        if (isDisplayable() && MouseUtils.mouseWithinBounds(mouseX, mouseY, x, y, x + width, y + 20F))
            value.set(!value.get())
    }
}