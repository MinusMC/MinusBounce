/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.RenderUtils
import java.awt.Color

class GuiBackground(private val prevGui: GuiScreen): GuiScreen() {
    override fun initGui() {
        buttonList.add(BoolButton(0, width / 2 + 100, height / 2 - 60, enabled))
        buttonList.add(BoolButton(1, width / 2 + 100, height / 2 - 30, particles))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        RenderUtils.drawRoundedRect((width / 2 - 200).toFloat(), (height / 2 - 120).toFloat(), (width / 2 + 200).toFloat(), (height / 2 + 120).toFloat(), 4f, Color(249, 246, 238, 120).rgb)
        Fonts.font72.drawCenteredString("Background settings", width.toFloat() / 2f, (height / 2 - mc.fontRendererObj.FONT_HEIGHT - 90).toFloat(), Color(54, 69, 79).rgb, false)
        Fonts.font50.drawString("Background", (width / 2 - 140).toFloat(), (height / 2 - 56).toFloat(), Color(54, 69, 79).rgb, false)
        Fonts.font50.drawString("Particles", (width / 2 - 140).toFloat(), (height / 2 - 26).toFloat(), Color(54, 69, 79).rgb, false)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> {
                val button = button as BoolButton
                button.state = !button.state
                enabled = button.state
            }
            1 -> {
                val button = button as BoolButton
                button.state = !button.state
                particles = button.state
            }
        }
    }

    companion object {
        var enabled = true
        var particles = false
    }

}


class BoolButton(buttonId: Int, x: Int, y: Int, var state: Boolean): GuiButton(buttonId, x, y, "") {
    constructor(buttonId: Int, x: Int, y: Int): this(buttonId, x, y, false)

    init {
        width = 32
        height = 15
    }

    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        RenderUtils.drawRoundedRect(xPosition.toFloat(), yPosition.toFloat(), (xPosition + width).toFloat(), (yPosition + height).toFloat(), height / 2f, Color(4, 217, 255).rgb)

        if (state) {
            RenderUtils.drawFilledCircle(xPosition.toFloat() + width / 4 * 3, yPosition.toFloat() + height / 2 + 0.5f, (height / 2).toFloat(), Color(54, 69, 79))
        } else {
            RenderUtils.drawFilledCircle(
                xPosition.toFloat() + width / 4,
                yPosition.toFloat() + height / 2 + 0.5f,
                (height / 2).toFloat(),
                Color(54, 69, 79)
            )
        }
    }
}