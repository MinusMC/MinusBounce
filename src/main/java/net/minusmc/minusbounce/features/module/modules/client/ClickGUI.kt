/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.client

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.client.clickgui.DropDownClickGui
import net.minusmc.minusbounce.ui.client.clickgui.styles.StyleMode
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.input.Keyboard
import java.awt.Color

@ModuleInfo(name = "ClickGUI", description = "Opens the ClickGUI.", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_RSHIFT, forceNoSound = true, onlyEnable = true)
object ClickGUI: Module() {
    val styleClazzes = ClassUtils.resolvePackage("net.minusmc.minusbounce.ui.client.clickgui.styles", StyleMode::class.java)
    var styles = styleClazzes.map {it.newInstance() as StyleMode}.sortedBy { it.styleName }

    val style: StyleMode
        get() = styles.find {styleValue.get().equals(it.styleName, true)} ?: throw NullPointerException()

    private val styleValue: ListValue = object: ListValue("Style", styles.map {it.styleName}.toTypedArray(), "LiquidBounce") {
        override fun onChanged(oldValue: String, newValue: String) = changeClickGui()
    }

    val fastRenderValue = BoolValue("FastRender", true)

    val scaleValue = FloatValue("Scale", 1F, 0.4F, 2F)
    val maxElementsValue = IntegerValue("MaxElements", 15, 1, 20)

    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Sky", "Rainbow", "LiquidSlowly", "Fade"), "Custom")
    private val colorRedValue = IntegerValue("Red", 0, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 160, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1F, 0F, 1F)
    private val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)

    val backgroundValue = ListValue("Background", arrayOf("Default", "Gradient", "None"), "Default")

    val gradStartValue = IntegerValue("GradientStartAlpha", 255, 0, 255) {
        backgroundValue.get().equals("gradient", true)
    }
    val gradEndValue = IntegerValue("GradientEndAlpha", 0, 0, 255) { backgroundValue.get().equals("gradient", true) }

    val animationValue = ListValue("Animation", arrayOf("Azura", "Slide", "SlideBounce", "Zoom", "ZoomBounce", "None"), "Azura")
    val animSpeedValue = FloatValue("AnimSpeed", 1F, 0.01F, 5F, "x")

    val accentColor: Color?
        get() = when (colorModeValue.get().lowercase()) {
            "custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
            "rainbow" -> Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0))
            "sky" -> RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
            "liquidslowly" -> ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
            "fade" -> ColorUtils.fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
            else -> null
        }

    fun changeClickGui() {
        if (style is DropDownClickGui) {
            val dropDown = style as DropDownClickGui
            dropDown.progress = 0.0
            dropDown.slide = 0.0
            dropDown.lastMS = System.currentTimeMillis()
        }
        mc.displayGuiScreen(style)
    }

    override fun onEnable() {
        changeClickGui()
    }
}
