/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.Render
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.plugin.PluginGuiManager
import net.minusmc.minusbounce.ui.client.altmanager.GuiAltManager
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.ui.font.GameFontRenderer
import net.minusmc.minusbounce.utils.render.*
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.animation.EaseInOutTimer
import net.minusmc.minusbounce.utils.login.UserUtils.isValidTokenOffline
import net.minusmc.minusbounce.utils.geometry.Rectagle
import java.awt.Color
import org.lwjgl.opengl.GL11.*

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    private val logoAnimation = EaseInOutTimer()

    override fun initGui() {
        buttonList.add(MainButton(0, 20, height / 2 - 45, "Singleplayer", ResourceLocation("minusbounce/menu/singleplayer.png")))
        buttonList.add(MainButton(1, 20, height / 2 + 5, "Multiplayer", ResourceLocation("minusbounce/menu/multiplayer.png")))
        buttonList.add(MainButton(4, 110, height - 46, 80, 26, "Settings", Fonts.fontLexend30, ResourceLocation("minusbounce/menu/settings.png")))
        buttonList.add(MainButton(6, 20, height - 46, 80, 26, "Client options", Fonts.fontLexend30, ResourceLocation("minusbounce/menu/client_settings.png")))
        buttonList.add(MainButton(5, width - 80, height - 46, 60, 26, "Exit", Fonts.fontLexend35, ResourceLocation("minusbounce/menu/exit.png")))
        
        buttonList.add(AltManagerButton(2, width - 210, 15))
        
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        logoAnimation.update()
        val easeProgress = EaseUtils.easeOutBack(logoAnimation.progress.toDouble())
        val deltaX = easeProgress * 36
        val deltaXText = easeProgress * 72
        val patternLogoBox = Rectagle(0, 0, 90, 50)

        drawLogoText("Minus", 20f + deltaXText.toFloat(), 15f, patternLogoBox)
        drawLogoText("Bounce", 20f + deltaXText.toFloat(), 30f + mc.fontRendererObj.FONT_HEIGHT, patternLogoBox)
        RenderUtils.drawImage(ResourceLocation("minusbounce/big.png"), 72 - deltaX.toInt(), 15, 45, 45)
        
        Gui.drawRect(0, 0, 0, 0, Integer.MIN_VALUE)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiAltManager(this))
            4 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            5 -> mc.shutdown()
            6 -> mc.displayGuiScreen(GuiClientSettings(this))
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}

    private fun drawLogoText(text: String, x: Float, y: Float, box: Rectagle) {
        var currentX = x
        val currentY = y
        for (char in text) {
            val charWidth = Fonts.fontSatoshiBold80.getCharWidth(char)

            if (!box.isMouseHover(currentX, currentY))
                Fonts.fontSatoshiBold80.drawString(char.toString(), currentX, currentY, Color.WHITE.rgb, true)
            
            currentX += charWidth
        }
    }
}

class MainButton(buttonId: Int, x: Int, y: Int, w: Int, h: Int, buttonText: String, val font: GameFontRenderer, val image: ResourceLocation): GuiButton(buttonId, x, y, buttonText) {
    constructor(buttonId: Int, x: Int, y: Int, buttonText: String, image: ResourceLocation): this(buttonId, x, y, 170, 40, buttonText, Fonts.fontLexend50, image)

    init {
        width = w
        height = h
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        val currentHover = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height

        RenderUtils.drawRoundedGradientRectCorner(xPosition, yPosition, xPosition + width, yPosition + height, 6f, Color(127, 127, 213, 180).rgb, Color(145, 234, 228, 180).rgb)
        RenderUtils.drawImage(image, xPosition + 4, yPosition + 4, height - 8, height - 8)

        GlStateManager.resetColor()
        font.drawString(displayString, xPosition + height.toFloat(), yPosition + (height - font.FONT_HEIGHT) / 2f + 2, Color.WHITE.rgb, false)
    }
}

class AltManagerButton(buttonId: Int, x: Int, y: Int): GuiButton(buttonId, x, y, "") {
    private val image = ResourceLocation("minusbounce/menu/altmanager.png")
    private val editImage = ResourceLocation("minusbounce/menu/edit.png")
    
    init {
        width = 180
        height = 40
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        RenderUtils.drawRoundedGradientRectCorner(xPosition, yPosition, xPosition + width, yPosition + height, 6f, Color(127, 127, 213, 180).rgb, Color(145, 234, 228, 180).rgb)
        RenderUtils.drawImage(image, xPosition + 6, yPosition + 6, height - 12, height - 12)

        GlStateManager.resetColor()
        Fonts.fontLexend50.drawString(mc.session.username, xPosition + 5f + height.toFloat(), yPosition + 12f, Color.WHITE.rgb, false)
        val stringValidAccount = if (isValidTokenOffline(mc.session.token)) "Premium" else "Cracked"
        Fonts.fontLexend35.drawString(stringValidAccount, xPosition + 5f + height.toFloat(), yPosition + 30f, Color.WHITE.rgb, false)

        RenderUtils.drawImage(editImage, xPosition + width - 29, yPosition + 17, 16, 16)
    }
}