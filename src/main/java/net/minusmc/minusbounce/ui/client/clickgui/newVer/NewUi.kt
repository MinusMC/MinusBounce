/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.newVer

import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.util.MathHelper
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI.accentColor
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI.fastRenderValue
import net.minusmc.minusbounce.ui.client.clickgui.newVer.element.CategoryElement
import net.minusmc.minusbounce.ui.client.clickgui.newVer.element.SearchElement
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.AnimationUtils
import net.minusmc.minusbounce.utils.MouseUtils.mouseWithinBounds
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.Stencil
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException
import java.util.function.Consumer

// clickgui rieng ko th
class NewUi private constructor() : GuiScreen() {
    private val categoryElements: MutableList<CategoryElement> = ArrayList()
    private var startYAnim = height / 2f
    private var endYAnim = height / 2f
    private var searchElement: SearchElement? = null
    private var fading = 0f

    init {
        for (c in ModuleCategory.values()) categoryElements.add(CategoryElement(c))
        categoryElements[0].focused = true
    }

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        for (ce in categoryElements) {
            for (me in ce.moduleElements) {
                if (me.listeningKeybind()) me.resetState()
            }
        }
        searchElement = SearchElement(40f, 115f, 180f, 20f)
        super.initGui()
    }

    override fun onGuiClosed() {
        for (ce in categoryElements) {
            if (ce.focused) ce.handleMouseRelease(-1, -1, 0, 0f, 0f, 0f, 0f)
        }
        Keyboard.enableRepeatEvents(false)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // will draw reduced ver once it gets under 1140x780.
        drawFullSized(mouseX, mouseY, partialTicks, accentColor)
    }

    private fun drawFullSized(mouseX: Int, mouseY: Int, partialTicks: Float, accentColor: Color?) {
        RenderUtils.originalRoundedRect(30f, 30f, width - 30f, height - 30f, 8f, -0xefeff0)
        // something to make it look more like windoze
        if (mouseWithinBounds(
                mouseX,
                mouseY,
                width - 54f,
                30f,
                width - 30f,
                50f
            )
        ) fading += 0.2f * RenderUtils.deltaTime * 0.045f else fading -= 0.2f * RenderUtils.deltaTime * 0.045f
        fading = MathHelper.clamp_float(fading, 0f, 1f)
        RenderUtils.customRounded(width - 54f, 30f, width - 30f, 50f, 0f, 8f, 0f, 8f, Color(1f, 0f, 0f, fading).rgb)
        GlStateManager.disableAlpha()
        RenderUtils.drawImage(IconManager.removeIcon, width - 47, 35, 10, 10)
        GlStateManager.enableAlpha()
        Stencil.write(true)
        RenderUtils.drawFilledCircle(65f, 80f, 25f, Color(45, 45, 45))
        Stencil.erase(true)
        if (mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID) != null) {
            val skin = mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID).locationSkin
            GL11.glPushMatrix()
            GL11.glTranslatef(40f, 55f, 0f)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDepthMask(false)
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            GL11.glColor4f(1f, 1f, 1f, 1f)
            mc.textureManager.bindTexture(skin)
            drawScaledCustomSizeModalRect(
                0, 0, 8f, 8f, 8, 8, 50, 50,
                64f, 64f
            )
            GL11.glDepthMask(true)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glPopMatrix()
        }
        Stencil.dispose()
        if (Fonts.fontLarge.getStringWidth(mc.thePlayer.gameProfile.name) > 70) Fonts.fontLarge.drawString(
            Fonts.fontLarge.trimStringToWidth(
                mc.thePlayer.gameProfile.name,
                50
            ) + "...", 100, 78 - Fonts.fontLarge.FONT_HEIGHT + 15, -1
        ) else Fonts.fontLarge.drawString(mc.thePlayer.gameProfile.name, 100, 78 - Fonts.fontLarge.FONT_HEIGHT + 15, -1)
        if (searchElement!!.drawBox(mouseX, mouseY, accentColor!!)) {
            searchElement!!.drawPanel(
                mouseX,
                mouseY,
                230f,
                50f,
                (width - 260).toFloat(),
                (height - 80).toFloat(),
                Mouse.getDWheel(),
                categoryElements,
                accentColor
            )
            return
        }
        val elementHeight = 24f
        var startY = 140f
        for (ce in categoryElements) {
            ce.drawLabel(mouseX, mouseY, 30f, startY, 200f, elementHeight)
            if (ce.focused) {
                startYAnim = if (fastRenderValue.get()) startY + 6f else AnimationUtils.animate(
                    startY + 6f,
                    startYAnim,
                    (if (startYAnim - (startY + 5f) > 0) 0.65f else 0.55f) * RenderUtils.deltaTime * 0.025f
                )
                endYAnim = if (fastRenderValue.get()) startY + elementHeight - 6f else AnimationUtils.animate(
                    startY + elementHeight - 6f,
                    endYAnim,
                    (if (endYAnim - (startY + elementHeight - 5f) < 0) 0.65f else 0.55f) * RenderUtils.deltaTime * 0.025f
                )
                ce.drawPanel(
                    mouseX, mouseY, 230f, 50f, (width - 260).toFloat(), (height - 80).toFloat(), Mouse.getDWheel(),
                    accentColor
                )
            }
            startY += elementHeight
        }
        RenderUtils.originalRoundedRect(32f, startYAnim, 34f, endYAnim, 1f, accentColor.rgb)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseWithinBounds(mouseX, mouseY, width - 54f, 30f, width - 30f, 50f)) {
            mc.displayGuiScreen(null)
            return
        }
        val elementHeight = 24f
        var startY = 140f
        searchElement!!.handleMouseClick(
            mouseX,
            mouseY,
            mouseButton,
            230f,
            50f,
            (width - 260).toFloat(),
            (height - 80).toFloat(),
            categoryElements
        )
        if (!searchElement!!.isTyping()) for (ce in categoryElements) {
            if (ce.focused) ce.handleMouseClick(
                mouseX,
                mouseY,
                mouseButton,
                230f,
                50f,
                (width - 260).toFloat(),
                (height - 80).toFloat()
            )
            if (mouseWithinBounds(
                    mouseX,
                    mouseY,
                    30f,
                    startY,
                    230f,
                    startY + elementHeight
                ) && !searchElement!!.isTyping()
            ) {
                categoryElements.forEach(Consumer { e: CategoryElement -> e.focused = false })
                ce.focused = true
                return
            }
            startY += elementHeight
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        for (ce in categoryElements) {
            if (ce.focused) {
                if (ce.handleKeyTyped(typedChar, keyCode)) return
            }
        }
        if (searchElement!!.handleTyping(
                typedChar,
                keyCode,
                230f,
                50f,
                (width - 260).toFloat(),
                (height - 80).toFloat(),
                categoryElements
            )
        ) return
        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        searchElement!!.handleMouseRelease(
            mouseX,
            mouseY,
            state,
            230f,
            50f,
            (width - 260).toFloat(),
            (height - 80).toFloat(),
            categoryElements
        )
        if (!searchElement!!.isTyping()) for (ce in categoryElements) {
            if (ce.focused) ce.handleMouseRelease(
                mouseX,
                mouseY,
                state,
                230f,
                50f,
                (width - 260).toFloat(),
                (height - 80).toFloat()
            )
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    companion object {
        private var instance: NewUi? = null
        fun getInstance(): NewUi {
            return if (instance == null) NewUi().also {
                instance = it
            } else instance!!
        }

        fun resetInstance() {
            instance = NewUi()
        }
    }
}
