/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import net.minusmc.minusbounce.ui.client.clickgui.Panel
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.ButtonElement
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.ModuleElement
import net.minusmc.minusbounce.ui.client.clickgui.DropDownClickGui
import net.minusmc.minusbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.ui.font.GameFontRenderer
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.block.BlockUtils.getBlockName
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Mouse
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min
import net.minusmc.minusbounce.utils.MathUtils.round

class SlowlyStyle : DropDownClickGui("Slowly") {
    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel?) {
        RenderUtils.drawBorderedRect(panel!!.x.toFloat(), panel.y.toFloat() - 3, panel.x.toFloat() + panel.width, panel.y.toFloat() + 17, 3f, Color(0, 0, 0).rgb, Color(0, 0, 0).rgb)
        if (panel.getFade() > 0) {
            RenderUtils.drawBorderedRect(panel.x.toFloat(), panel.y.toFloat() + 17, panel.x.toFloat() + panel.width, (panel.y + 19 + panel.getFade()).toFloat(), 3f, Color(0, 0, 0).rgb, Color(0, 0, 0).rgb)
            RenderUtils.drawBorderedRect(panel.x.toFloat(), (panel.y + 17 + panel.getFade()).toFloat(), panel.x.toFloat() + panel.width, (panel.y + 19 + panel.getFade() + 5).toFloat(), 3f, Color(0, 0, 0).rgb, Color(0, 0, 0).rgb)
        }
        GlStateManager.resetColor()
        val textWidth = Fonts.font35.getStringWidth("§f" + StringUtils.stripControlCodes(panel.name)).toFloat()
        Fonts.font35.drawString(panel.name, (panel.x - (textWidth - 100.0f) / 2f).toInt(), panel.y + 7 - 3, Color.WHITE.rgb)
    }

    override fun drawDescription(mouseX: Int, mouseY: Int, text: String?) {
        val textWidth = Fonts.font35.getStringWidth(text!!)
        RenderUtils.drawBorderedRect((mouseX + 9).toFloat(), mouseY.toFloat(), (mouseX + textWidth + 14).toFloat(), (mouseY + Fonts.font35.FONT_HEIGHT + 3).toFloat(), 3f, Color(0, 0, 0).rgb, Color(0, 0, 0).rgb)
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, mouseX + 12, mouseY + Fonts.font35.FONT_HEIGHT / 2, Color.WHITE.rgb)
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement?) {
        Gui.drawRect(buttonElement!!.x - 1, buttonElement.y - 1, buttonElement.x + buttonElement.width + 1, buttonElement.y + buttonElement.height + 1, ColorUtils.hoverColor(if (buttonElement.color != Int.MAX_VALUE) Color(10, 10, 10) else Color(0, 0, 0), buttonElement.hoverTime).rgb)
        GlStateManager.resetColor()
        Fonts.font35.drawString(buttonElement.displayName, buttonElement.x + 5, buttonElement.y + 7, Color.WHITE.rgb)
    }

    override fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement?) {
        Gui.drawRect(moduleElement!!.x - 1, moduleElement.y - 1, moduleElement.x + moduleElement.width + 1, moduleElement.y + moduleElement.height + 1, ColorUtils.hoverColor(Color(40, 40, 40), moduleElement.hoverTime).rgb)
        Gui.drawRect(moduleElement.x - 1, moduleElement.y - 1, moduleElement.x + moduleElement.width + 1, moduleElement.y + moduleElement.height + 1, ColorUtils.hoverColor(Color(0, 0, 0, moduleElement.slowlyFade), moduleElement.hoverTime).rgb)
        GlStateManager.resetColor()
        Fonts.font35.drawString(moduleElement.displayName, moduleElement.x + 5, moduleElement.y + 7, Color.WHITE.rgb)
        drawValues(moduleElement, mouseX, mouseY)
    }

    override fun drawValues(moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val moduleValues = moduleElement.module.values
        if (moduleValues.isNotEmpty()) {
            Fonts.font35.drawString(">", moduleElement.x + moduleElement.width - 8, moduleElement.y + 5, Color.WHITE.rgb)
            if (moduleElement.isShowSettings) {
                if (moduleElement.settingsWidth > 0f && yPos > moduleElement.y + 6)
                    RenderUtils.drawBorderedRect((moduleElement.x + moduleElement.width + 4).toFloat(), (moduleElement.y + 6).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 2).toFloat(), 3f, Color(0, 0, 0).rgb, Color(0, 0, 0).rgb)
                yPos = moduleElement.y + 6
                for (value in moduleValues) {
                    if (!value.canDisplay.invoke()) continue
                    val isNumber = value.get() is Number
                    if (isNumber) {
                        assumeNonVolatile = false
                    }
                    when (value) {
                        is BoolValue -> drawBoolValue(value, moduleElement, mouseX, mouseY)
                        is ListValue -> drawListValue(value, moduleElement, mouseX, mouseY)
                        is FloatValue -> drawFloatValue(value, moduleElement, mouseX, mouseY)
                        is IntegerValue -> drawIntegerValue(value, moduleElement, mouseX, mouseY)
                        is FontValue -> drawFontValue(value, moduleElement, mouseX, mouseY)
                        is TextValue -> drawTextValue(value, moduleElement, mouseX, mouseY)
                        is IntRangeValue -> drawIntRangeValue(value, moduleElement, mouseX, mouseY)
                        is FloatRangeValue -> drawFloatRangeValue(value, moduleElement, mouseX, mouseY)
                    }
                    if (isNumber) {
                        assumeNonVolatile = true
                    }
                }
                moduleElement.updatePressed()
                mouseDown = Mouse.isButtonDown(0)
                rightMouseDown = Mouse.isButtonDown(1)
            }
        }
    }

    override fun drawBoolValue(value: BoolValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos && mouseY <= yPos + 12 && Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
            value.set(!value.get())
            MinecraftInstance.mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
        }
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 2, if (value.get()) Color.WHITE.rgb else Int.MAX_VALUE)
        yPos += 11
    }

    override fun drawListValue(value: ListValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 16) moduleElement.settingsWidth = textWidth + 16
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 2, 0xffffff)
        Fonts.font35.drawString(if (value.openList) "-" else "+", (moduleElement.x + moduleElement.width + moduleElement.settingsWidth - if (value.openList) 5 else 6).toInt(), yPos + 2, 0xffffff)
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos && mouseY <= yPos + Fonts.font35.FONT_HEIGHT && Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
            value.openList = !value.openList
            MinecraftInstance.mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
        }
        yPos += Fonts.font35.FONT_HEIGHT + 1
        for (valueOfList in value.values) {
            val textWidth2 = Fonts.font35.getStringWidth("> $valueOfList").toFloat()
            if (moduleElement.settingsWidth < textWidth2 + 12) moduleElement.settingsWidth = textWidth2 + 12
            if (value.openList) {
                if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14 && Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                    value.set(valueOfList)
                    MinecraftInstance.mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
                }
                GlStateManager.resetColor()
                Fonts.font35.drawString("> $valueOfList", moduleElement.x + moduleElement.width + 6, yPos + 2, if (value.get().equals(valueOfList, ignoreCase = true)) Color.WHITE.rgb else Int.MAX_VALUE)
                yPos += Fonts.font35.FONT_HEIGHT + 1
            }
        }
        if (!value.openList) {
            yPos += 1
        }
    }

    override fun drawFloatValue(value: FloatValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: " + round(value.get()) + value.suffix
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        val valueOfSlide = drawSlider(value.get(), value.minimum, value.maximum, false, moduleElement.x + moduleElement.width + 8, yPos + 14, moduleElement.settingsWidth.toInt() - 12, mouseX, mouseY, Color(120, 120, 120))
        if (valueOfSlide != value.get()) value.set(valueOfSlide)
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 3, 0xffffff)
        yPos += 19
    }

    override fun drawIntegerValue(value: IntegerValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: " + if (value is BlockValue) getBlockName(value.get()) + " (" + value.get() + ")" else value.get().toString() + value.suffix
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        val valueOfSlide = drawSlider(value.get().toFloat(), value.minimum.toFloat(), value.maximum.toFloat(), true, moduleElement.x + moduleElement.width + 8, yPos + 14, moduleElement.settingsWidth.toInt() - 12, mouseX, mouseY, Color(120, 120, 120))
        if (valueOfSlide != value.get().toFloat()) value.set(valueOfSlide.toInt())
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 3, 0xffffff)
        yPos += 19
    }

    override fun drawFontValue(value: FontValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val fontRenderer = value.get()
        var displayString = "Font: Unknown"
        if (fontRenderer is GameFontRenderer) { displayString = "Font: " + fontRenderer.defaultFont.font.name + " - " + fontRenderer.defaultFont.font.size
        } else if (fontRenderer === Fonts.minecraftFont) displayString = "Font: Minecraft" else {
            val objects = Fonts.getFontDetails(fontRenderer)
            if (objects != null) { displayString = objects[0].toString() + if (objects[1] as Int != -1) " - " + objects[1] else ""
            }
        }
        Fonts.font35.drawString(displayString, moduleElement.x + moduleElement.width + 6, yPos + 2, Color.WHITE.rgb
        )
        val stringWidth = Fonts.font35.getStringWidth(displayString)
        if (moduleElement.settingsWidth < stringWidth + 8) moduleElement.settingsWidth = (stringWidth + 8).toFloat()
        if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) && mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos && mouseY <= yPos + 12) {
            val fonts = Fonts.fonts
            if (Mouse.isButtonDown(0)) {
                var i = 0
                while (i < fonts.size) {
                    val font = fonts[i]
                    if (font === fontRenderer) {
                        i++
                        if (i >= fonts.size) i = 0
                        value.set(fonts[i])
                        break
                    }
                    i++
                }
            } else {
                var i = fonts.size - 1
                while (i >= 0) {
                    val font = fonts[i]
                    if (font === fontRenderer) {
                        i--
                        if (i >= fonts.size) i = 0
                        if (i < 0) i = fonts.size - 1
                        value.set(fonts[i])
                        break
                    }
                    i--
                }
            }
        }
        yPos += 11
    }

    override fun drawTextValue(value: TextValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: " + value.get()
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 12
    }

    override fun drawIntRangeValue(value: IntRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: ${value.get().getMin()} - ${value.get().getMax()}"
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 14).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 16).toFloat(), Color(120, 120, 120).rgb)
        val sliderMinValue = moduleElement.x + moduleElement.width + 8 + (moduleElement.settingsWidth - 12) * (value.get().getMin() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawFilledCircle(sliderMinValue.toInt(), yPos + 15, 3f, Color(120, 120, 120))
        val sliderMaxValue = moduleElement.x + moduleElement.width + 8 + (moduleElement.settingsWidth - 12) * (value.get().getMax() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawFilledCircle(sliderMaxValue.toInt(), yPos + 15, 3f, Color(120, 120, 120))
        val distBetMaxAndMin = (moduleElement.settingsWidth - 12) * (value.get().getMax() - value.get().getMin()) / (value.maximum - value.minimum)
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
            val dWheel = Mouse.getDWheel()

            if ((mouseX >= sliderMaxValue + 12 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4) || (mouseX >= sliderMaxValue - distBetMaxAndMin / 2 - 2 && mouseX <= sliderMaxValue + 14)) {
                if (Mouse.hasWheel() && dWheel != 0) {
                    if (dWheel > 0) value.setMaxValue(min((value.get().getMax() + 1).toDouble(), value.maximum.toDouble()))
                    if (dWheel < 0) value.setMaxValue(max((value.get().getMax() - 1).toDouble(), value.minimum.toDouble()))
                }
                if (Mouse.isButtonDown(0)) {
                    val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                    value.setMaxValue((value.minimum + (value.maximum - value.minimum) * i).toInt())
                }
            } else if ((mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= sliderMinValue + 11) || (mouseX >= sliderMinValue + 8 && mouseX <= sliderMinValue + distBetMaxAndMin / 2 - 2)) {
                if (Mouse.hasWheel() && dWheel != 0) {
                    if (dWheel > 0) value.setMinValue(min((value.get().getMin() + 1).toDouble(), value.maximum.toDouble()))
                    if (dWheel < 0) value.setMinValue(max((value.get().getMin() - 1).toDouble(), value.minimum.toDouble()))
                }
                if (Mouse.isButtonDown(0)) {
                    val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                    value.setMinValue((value.minimum + (value.maximum - value.minimum) * i).toInt())
                }
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    override fun drawFloatRangeValue(value: FloatRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: ${round(value.get().getMin())}${value.suffix} - ${round(value.get().getMax())}${value.suffix}"
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 14).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 16).toFloat(), Color(120, 120, 120).rgb)
        val sliderMinValue = moduleElement.x + moduleElement.width + 8 + (moduleElement.settingsWidth - 12) * (value.get().getMin() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawFilledCircle(sliderMinValue.toInt(), yPos + 15, 3f, Color(120, 120, 120))
        val sliderMaxValue = moduleElement.x + moduleElement.width + 8 + (moduleElement.settingsWidth - 12) * (value.get().getMax() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawFilledCircle(sliderMaxValue.toInt(), yPos + 15, 3f, Color(120, 120, 120))
        val distBetMaxAndMin = (moduleElement.settingsWidth - 12) * (value.get().getMax() - value.get().getMin()) / (value.maximum - value.minimum)
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
            val dWheel = Mouse.getDWheel()

            if ((mouseX >= sliderMaxValue + 12 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4) || (mouseX >= sliderMaxValue - distBetMaxAndMin / 2 - 2 && mouseX <= sliderMaxValue + 14)) {
                if (Mouse.hasWheel() && dWheel != 0) {
                    if (dWheel > 0) value.setMaxValue(min((value.get().getMax() + 0.01f).toDouble(), value.maximum.toDouble()))
                    if (dWheel < 0) value.setMaxValue(max((value.get().getMax() - 0.01f).toDouble(), value.minimum.toDouble()))
                }
                if (Mouse.isButtonDown(0)) {
                    val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                    value.setMaxValue(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()))
                }
            } else if ((mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= sliderMinValue + 11) || (mouseX >= sliderMinValue + 8 && mouseX <= sliderMinValue + distBetMaxAndMin / 2 - 2)) {
                if (Mouse.hasWheel() && dWheel != 0) {
                    if (dWheel > 0) value.setMinValue(min((value.get().getMin() + 0.01f).toDouble(), value.maximum.toDouble()))
                    if (dWheel < 0) value.setMinValue(max((value.get().getMin() - 0.01f).toDouble(), value.minimum.toDouble()))
                }
                if (Mouse.isButtonDown(0)) {
                    val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                    value.setMinValue(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()))
                }
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    private fun drawSlider(value: Float, min: Float, max: Float, inte: Boolean, x: Int, y: Int, width: Int, mouseX: Int, mouseY: Int, color: Color?): Float {
        val displayValue = max(min.toDouble(), min(value.toDouble(), max.toDouble())).toFloat()
        val sliderValue = x.toFloat() + width.toFloat() * (displayValue - min) / (max - min)
        RenderUtils.drawRect(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + 2).toFloat(), Int.MAX_VALUE)
        RenderUtils.drawRect(x.toFloat(), y.toFloat(), sliderValue, (y + 2).toFloat(), color!!)
        RenderUtils.drawFilledCircle(sliderValue.toInt(), y + 1, 3f, color)
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 3) {
            val dWheel = Mouse.getDWheel()
            if (Mouse.hasWheel() && dWheel != 0) {
                if (dWheel > 0) return min((value + if (inte) 1f else 0.01f).toDouble(), max.toDouble()).toFloat()
                if (dWheel < 0) return max((value - if (inte) 1f else 0.01f).toDouble(), min.toDouble()).toFloat()
            }
            if (Mouse.isButtonDown(0)) {
                val i = MathHelper.clamp_double((mouseX.toDouble() - x.toDouble()) / (width.toDouble() - 3), 0.0, 1.0)
                var bigDecimal = BigDecimal((min + (max - min) * i).toString())
                bigDecimal = bigDecimal.setScale(2, 4)
                return bigDecimal.toFloat()
            }
        }
        return value
    }
}