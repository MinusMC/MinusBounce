/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui

import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI.accentColor
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI.animSpeedValue
import net.minusmc.minusbounce.ui.client.clickgui.elements.ButtonElement
import net.minusmc.minusbounce.ui.client.clickgui.elements.ModuleElement
import net.minusmc.minusbounce.ui.client.clickgui.style.Style
import net.minusmc.minusbounce.ui.client.clickgui.style.styles.SlowlyStyle
import net.minusmc.minusbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.minusmc.minusbounce.utils.render.ColorUtils.reAlpha
import net.minusmc.minusbounce.utils.render.EaseUtils.easeOutBack
import net.minusmc.minusbounce.utils.render.EaseUtils.easeOutQuart
import net.minusmc.minusbounce.utils.render.RenderUtils
import org.lwjgl.input.Mouse
import java.io.IOException
import java.util.*

class ClickGui : GuiScreen() {
    val panels: MutableList<Panel> = ArrayList()
    var style: Style = SlowlyStyle()
    private var clickedPanel: Panel? = null
    private var mouseX = 0
    private var mouseY = 0
    var slide = 0.0
    var progress = 0.0
    var lastMS = System.currentTimeMillis()

    init {
        val width = 100
        val height = 18
        var yPos = 5
        for (category in ModuleCategory.values()) {
            panels.add(object : Panel(category.displayName, 100, yPos, width, height, false) {
                override fun setupItems() {
                    for (module in MinusBounce.moduleManager.modules) if (module.category === category) elements.add(
                        ModuleElement(module)
                    )
                }
            })
            yPos += 20
        }
        yPos += 20
    }

    override fun initGui() {
        progress = 0.0
        slide = progress
        lastMS = System.currentTimeMillis()
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        var mouseX = mouseX
        var mouseY = mouseY
        progress =
            if (progress < 1) ((System.currentTimeMillis() - lastMS).toFloat() / (500f / animSpeedValue.get())).toDouble() // fully fps async
            else 1.0
        when (MinusBounce.moduleManager[ClickGUI::class.java]!!.animationValue.get()
            .lowercase(
                Locale.getDefault()
            )) {
            "slidebounce", "zoombounce" -> slide = easeOutBack(progress)
            "slide", "zoom", "azura" -> slide = easeOutQuart(progress)
            "none" -> slide = 1.0
        }

        // Enable DisplayList optimization
        assumeNonVolatile = true
        val scale = MinusBounce.moduleManager[ClickGUI::class.java]!!.scaleValue.get().toDouble()
        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()
        this.mouseX = mouseX
        this.mouseY = mouseY
        when (MinusBounce.moduleManager[ClickGUI::class.java]!!.backgroundValue.get()) {
            "Default" -> drawDefaultBackground()
            "Gradient" -> drawGradientRect(
                0, 0, width, height,
                reAlpha(
                    accentColor!!, MinusBounce.moduleManager[ClickGUI::class.java]!!.gradEndValue.get()
                ).rgb,
                reAlpha(
                    accentColor!!, MinusBounce.moduleManager[ClickGUI::class.java]!!.gradStartValue.get()
                ).rgb
            )

            else -> {}
        }
        GlStateManager.disableAlpha()
        GlStateManager.enableAlpha()
        when (MinusBounce.moduleManager[ClickGUI::class.java]!!.animationValue.get()
            .lowercase(
                Locale.getDefault()
            )) {
            "azura" -> {
                GlStateManager.translate(0.0, (1.0 - slide) * height * 2.0, 0.0)
                GlStateManager.scale(scale, scale + (1.0 - slide) * 2.0, scale)
            }

            "slide", "slidebounce" -> {
                GlStateManager.translate(0.0, (1.0 - slide) * height * 2.0, 0.0)
                GlStateManager.scale(scale, scale, scale)
            }

            "zoom" -> {
                GlStateManager.translate(
                    (1.0 - slide) * (width / 2.0),
                    (1.0 - slide) * (height / 2.0),
                    (1.0 - slide) * (width / 2.0)
                )
                GlStateManager.scale(scale * slide, scale * slide, scale * slide)
            }

            "zoombounce" -> {
                GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), 0.0)
                GlStateManager.scale(scale * slide, scale * slide, scale * slide)
            }

            "none" -> GlStateManager.scale(scale, scale, scale)
        }
        for (panel in panels) {
            panel.updateFade(RenderUtils.deltaTime)
            panel.drawScreen(mouseX, mouseY, partialTicks)
        }
        for (panel in panels) {
            for (element in panel.elements) {
                if (element is ModuleElement) {
                    if (mouseX != 0 && mouseY != 0 && element.isHovering(
                            mouseX,
                            mouseY
                        ) && element.isVisible && element.y <= panel.y + panel.getFade()
                    ) style.drawDescription(mouseX, mouseY, element.module.description)
                }
            }
        }
        GlStateManager.disableLighting()
        RenderHelper.disableStandardItemLighting()
        when (MinusBounce.moduleManager[ClickGUI::class.java]!!.animationValue.get()
            .lowercase(
                Locale.getDefault()
            )) {
            "azura" -> GlStateManager.translate(0.0, (1.0 - slide) * height * -2.0, 0.0)
            "slide", "slidebounce" -> GlStateManager.translate(0.0, (1.0 - slide) * height * -2.0, 0.0)
            "zoom" -> GlStateManager.translate(
                -1 * (1.0 - slide) * (width / 2.0),
                -1 * (1.0 - slide) * (height / 2.0),
                -1 * (1.0 - slide) * (width / 2.0)
            )

            "zoombounce" -> GlStateManager.translate(
                -1 * (1.0 - slide) * (width / 2.0),
                -1 * (1.0 - slide) * (height / 2.0),
                0.0
            )
        }
        GlStateManager.scale(1f, 1f, 1f)
        assumeNonVolatile = false
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        val wheel = Mouse.getEventDWheel()
        for (i in panels.indices.reversed()) if (panels[i].handleScroll(mouseX, mouseY, wheel)) break
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        var mouseX = mouseX
        var mouseY = mouseY
        val scale = MinusBounce.moduleManager[ClickGUI::class.java]!!.scaleValue.get().toDouble()
        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()
        for (i in panels.indices.reversed()) {
            if (panels[i].mouseClicked(mouseX, mouseY, mouseButton)) {
                break
            }
        }
        for (panel in panels) {
            panel.drag = false
            if (mouseButton == 0 && panel.isHovering(mouseX, mouseY)) {
                clickedPanel = panel
                break
            }
        }
        if (clickedPanel != null) {
            clickedPanel!!.x2 = clickedPanel!!.x - mouseX
            clickedPanel!!.y2 = clickedPanel!!.y - mouseY
            clickedPanel!!.drag = true
            panels.remove(clickedPanel)
            panels.add(clickedPanel!!)
            clickedPanel = null
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        var mouseX = mouseX
        var mouseY = mouseY
        val scale = MinusBounce.moduleManager[ClickGUI::class.java]!!.scaleValue.get().toDouble()
        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()
        for (panel in panels) {
            panel.mouseReleased(mouseX, mouseY, state)
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun updateScreen() {
        for (panel in panels) {
            for (element in panel.elements) {
                if (element is ButtonElement) {
                    if (element.isHovering(mouseX, mouseY)) {
                        if (element.hoverTime < 7) element.hoverTime++
                    } else if (element.hoverTime > 0) element.hoverTime--
                }
                if (element is ModuleElement) {
                    if (element.module.state) {
                        if (element.slowlyFade < 255) element.slowlyFade += 50
                    } else if (element.slowlyFade > 0) element.slowlyFade -= 50
                    if (element.slowlyFade > 255) element.slowlyFade = 255
                    if (element.slowlyFade < 0) element.slowlyFade = 0
                }
            }
        }
        super.updateScreen()
    }

    override fun onGuiClosed() {
        MinusBounce.fileManager.saveConfig(MinusBounce.fileManager.clickGuiConfig)
        MinusBounce.fileManager.saveConfig(MinusBounce.fileManager.valuesConfig)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}
