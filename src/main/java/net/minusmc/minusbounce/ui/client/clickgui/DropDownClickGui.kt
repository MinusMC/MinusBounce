package net.minusmc.minusbounce.ui.client.clickgui

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.ui.client.clickgui.styles.StyleMode
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.ButtonElement
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.ModuleElement
import net.minusmc.minusbounce.ui.font.AWTFontRenderer
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.EaseUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Mouse
import java.io.IOException
import java.util.*


abstract class DropDownClickGui(styleName: String): StyleMode(styleName) {

    open val panels: MutableList<Panel> = ArrayList()
    private var clickedPanel: Panel? = null
    private var mouseX = 0
    private var mouseY = 0
    var slide = 0.0
    var progress = 0.0
    var lastMS = System.currentTimeMillis()

    var mouseDown = false
    var rightMouseDown = false
    val guiColor: Int
        get() = ClickGUI.accentColor!!.rgb

    var yPos = 0

    init {
        val width = 100
        val height = 18
        var yPos = 5
        for (category in ModuleCategory.values()) {
            panels.add(object : Panel(category.displayName, 100, yPos, width, height, false) {
                override fun setupItems() {
                    MinusBounce.moduleManager.modules.filter { it.category === category }.forEach { elements.add(ModuleElement(it)) }
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
        val clickGuiModule = MinusBounce.moduleManager[ClickGUI::class.java]!!

        var mouseX = mouseX
        var mouseY = mouseY
        progress = if (progress < 1) ((System.currentTimeMillis() - lastMS).toFloat() / (500f / ClickGUI.animSpeedValue.get())).toDouble() else 1.0
        when (clickGuiModule.animationValue.get().lowercase()) {
            "slidebounce", "zoombounce" -> slide = EaseUtils.easeOutBack(progress)
            "slide", "zoom", "azura" -> slide = EaseUtils.easeOutQuart(progress)
            "none" -> slide = 1.0
        }

        // Enable DisplayList optimization
        AWTFontRenderer.assumeNonVolatile = true
        val scale = clickGuiModule.scaleValue.get().toDouble()
        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()
        this.mouseX = mouseX
        this.mouseY = mouseY
        when (clickGuiModule.backgroundValue.get()) {
            "Default" -> drawDefaultBackground()
            "Gradient" -> drawGradientRect(0, 0, width, height, ColorUtils.reAlpha(ClickGUI.accentColor!!, clickGuiModule.gradEndValue.get()).rgb, ColorUtils.reAlpha(ClickGUI.accentColor!!, clickGuiModule.gradStartValue.get()).rgb)
        }
        GlStateManager.disableAlpha()
        GlStateManager.enableAlpha()
        when (clickGuiModule.animationValue.get().lowercase()) {
            "azura" -> {
                GlStateManager.translate(0.0, (1.0 - slide) * height * 2.0, 0.0)
                GlStateManager.scale(scale, scale + (1.0 - slide) * 2.0, scale)
            }
            "slide", "slidebounce" -> {
                GlStateManager.translate(0.0, (1.0 - slide) * height * 2.0, 0.0)
                GlStateManager.scale(scale, scale, scale)
            }
            "zoom" -> {
                GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), (1.0 - slide) * (width / 2.0))
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
                    ) drawDescription(mouseX, mouseY, element.module.description)
                }
            }
        }

        GlStateManager.disableLighting()
        RenderHelper.disableStandardItemLighting()
        when (clickGuiModule.animationValue.get().lowercase()) {
            "azura" -> GlStateManager.translate(0.0, (1.0 - slide) * height * -2.0, 0.0)
            "slide", "slidebounce" -> GlStateManager.translate(0.0, (1.0 - slide) * height * -2.0, 0.0)
            "zoom" -> GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), -1 * (1.0 - slide) * (width / 2.0))
            "zoombounce" -> GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), 0.0)
        }
        GlStateManager.scale(1f, 1f, 1f)
        AWTFontRenderer.assumeNonVolatile = false
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
        panels.forEach {it.mouseReleased(mouseX, mouseY, state)}

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

    abstract fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel?)
    abstract fun drawDescription(mouseX: Int, mouseY: Int, text: String?)
    abstract fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement?)
    abstract fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement?)


    abstract fun drawValues(moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    abstract fun drawBoolValue(value: BoolValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    abstract fun drawListValue(value: ListValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    abstract fun drawFloatValue(value: FloatValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    abstract fun drawIntegerValue(value: IntegerValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    abstract fun drawFontValue(value: FontValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    abstract fun drawTextValue(value: TextValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    abstract fun drawIntRangeValue(value: IntRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    abstract fun drawFloatRangeValue(value: FloatRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
}