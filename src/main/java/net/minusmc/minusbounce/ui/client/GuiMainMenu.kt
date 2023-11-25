/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.altmanager.GuiAltManager
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    private val buttons = hashMapOf<Int, Class<out GuiScreen>>()

    override fun initGui() {
        val defaultHeight = (this.height / 2.5).toInt()

        this.buttonList.add(MainMenuButton(0, this.width / 2 - 148, defaultHeight, "Singleplayer"))
        this.buttonList.add(MainMenuButton(1, this.width / 2 + 32, defaultHeight, "Multiplayer"))
        this.buttonList.add(MainMenuButton(2, this.width / 2 - 148, defaultHeight + 45, "Alt manager"))
        this.buttonList.add(MainMenuButton(3, this.width / 2 + 32, defaultHeight + 45, "Mods and plugins"))
        this.buttonList.add(CircleButton(4, this.width - 80, 8, "Options", ResourceLocation("minusbounce/menu/settings.png")))
        this.buttonList.add(CircleButton(5, this.width - 40, 8, "Quit", ResourceLocation("minusbounce/menu/quit.png")))
        this.buttonList.add(CircleButton(6, this.width - 120, 8, "Background", ResourceLocation("minusbounce/menu/wallpaper.png")))


        var id = 201
        MinusBounce.mainMenuButton.forEach {
            val width = this.width / 2 + when (id % 2) {
                0 -> 32
                else -> -148
            }
            val height = defaultHeight + 45 * 2 + 45 * ((id - 201) / 2)
            this.buttonList.add(MainMenuButton(id, width, height, it.key))
            buttons[id] = it.value
            id++
        }

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        val bHeight = (this.height / 3.5).toInt()

        Fonts.font72.drawCenteredString(MinusBounce.CLIENT_NAME, (width / 2).toFloat(), (bHeight - 20).toFloat(), Color.WHITE.rgb, false)
        Gui.drawRect(0, 0, 0, 0, Integer.MIN_VALUE)
        Fonts.font40.drawString("Version: ${MinusBounce.CLIENT_VERSION}", 3F, (height - mc.fontRendererObj.FONT_HEIGHT * 2 - 4).toFloat(), 0xffffff, false)
        Fonts.font40.drawString("Made by ${MinusBounce.CLIENT_CREATOR}", 3F, (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        val creditInfo = "Copyright Mojang AB. Do not distribute!"
        Fonts.font40.drawString(creditInfo, width - 2f - Fonts.font40.getStringWidth(creditInfo), (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiAltManager(this))
            3 -> mc.displayGuiScreen(GuiModList(this))
            4 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            5 -> mc.shutdown()
            6 -> mc.displayGuiScreen(GuiBackground(this))
            else -> {
                val clazzButton = buttons[button.id] ?: return
                mc.displayGuiScreen(clazzButton.getConstructor(GuiScreen::class.java).newInstance(this) as GuiScreen)
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}

class CircleButton(buttonId: Int, x: Int, y: Int, buttonText: String, private val image: ResourceLocation): GuiButton(buttonId, x, y, buttonText) {
    private val radius = 15f
    init {
        width = radius.toInt() * 2
        height = radius.toInt() * 2
    }
    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.blendFunc(770, 771)
        RenderUtils.drawFilledCircle(xPosition.toFloat() + radius, yPosition.toFloat() + radius, radius, Color(249, 246, 238, 220))
        RenderUtils.drawCircle(xPosition.toFloat() + radius, yPosition.toFloat() + radius, radius, 2f, 0, 360, Color(54, 69, 79))
        GL11.glPushMatrix()
        GL11.glPushAttrib(1048575)
        GL11.glScaled(1.0, 1.0, 1.0)
        RenderUtils.drawImage(image, xPosition + radius.toInt() / 2, yPosition + radius.toInt() / 2, 16, 16)
        GL11.glPopAttrib()
        GL11.glPopMatrix()
    }
}

class MainMenuButton(buttonId: Int, x: Int, y: Int, buttonText: String): GuiButton(buttonId, x, y, buttonText) {
    init {
        width = 110
        height = 25
    }

    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        RenderUtils.drawRoundedRect(xPosition.toFloat(), yPosition.toFloat(), (xPosition + width).toFloat(), (yPosition + height).toFloat(), 4f, Color(249, 246, 238, 220).rgb)
        Fonts.font50.drawCenteredString(displayString, xPosition.toFloat() + width.toFloat() / 2f, yPosition.toFloat() + height.toFloat() / 2 - mc!!.fontRendererObj.FONT_HEIGHT / 2, Color(54, 69, 79).rgb, false)
    }
}