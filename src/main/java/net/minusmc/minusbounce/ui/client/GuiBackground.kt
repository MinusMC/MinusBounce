/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minusmc.minusbounce.ui.font.Fonts
import java.awt.Color

class GuiBackground(private val prevGui: GuiScreen): GuiScreen() {

    override fun initGui() {
        buttonList.add(GuiButton(0, width / 2 + 30, height / 2 - 50, "ON"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        Fonts.font40.drawString("Background", (width / 2 - 20).toFloat(), (height / 2 - 50 - mc.fontRendererObj.FONT_HEIGHT).toFloat(), Color.WHITE.rgb, false)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    companion object {
        var enabled = true
        var particles = false
    }

}
