/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.minusmc.minusbounce.ui.client

import net.minecraft.client.gui.*
import net.minecraftforge.fml.client.GuiModList
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.ui.client.altmanager.GuiAltManager
import net.minusmc.minusbounce.ui.font.Fonts
import net.minecraft.client.Minecraft
import java.awt.Color

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    override fun initGui() {
        val defaultHeight = (this.height / 2.5).toInt()

        this.buttonList.add(MainMenuButton(1, this.width / 2 - 120, defaultHeight, "Singleplayer"))
        this.buttonList.add(MainMenuButton(2, this.width / 2 + 20, defaultHeight, "Multiplayer"))
        this.buttonList.add(MainMenuButton(100, this.width / 2 - 120, defaultHeight + 30, "Alt manager"))
        this.buttonList.add(MainMenuButton(103, this.width / 2 + 20, defaultHeight + 30, "Mods and plugins"))
        this.buttonList.add(MainMenuButton(0, this.width / 2 - 120, defaultHeight + 30 * 2, "Options"))
        this.buttonList.add(MainMenuButton(4, this.width / 2 + 20, defaultHeight + 30 * 2, "Quit"))

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        val bHeight = (this.height / 3.5).toInt()

        Fonts.font72.drawCenteredString(MinusBounce.CLIENT_NAME, (width / 2).toFloat(), (bHeight - 20).toFloat(), Color.WHITE.rgb, false)
        Gui.drawRect(0, 0, 0, 0, Integer.MIN_VALUE)
        Fonts.font40.drawString("Version: ${MinusBounce.CLIENT_VERSION}", 3F, (height - mc.fontRendererObj.FONT_HEIGHT * 2).toFloat(), 0xffffff, false)
        Fonts.font40.drawString("Made by ${MinusBounce.CLIENT_CREATOR}", 3F, (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        val creditInfo = "Copyright Mojang AB. Do not distribute!"
        Fonts.font40.drawString(creditInfo, width - 2f - Fonts.font40.getStringWidth(creditInfo), (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            103 -> mc.displayGuiScreen(GuiModList(this))
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}

class MainMenuButton(val buttonId: Int, x: Int, y: Int, buttonText: String): GuiButton(buttonId, x, y, buttonText) {
    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        RenderUtils.drawRoundedRect(xPosition.toFloat(), yPosition.toFloat(), xPosition.toFloat() + 100f, yPosition.toFloat() + 20f, 3f, Color(237, 234, 222).rgb)
        Fonts.font40.drawCenteredString(displayString, (xPosition + 50).toFloat(), (yPosition + 8).toFloat(), Color.BLACK.rgb, false)
    }
}