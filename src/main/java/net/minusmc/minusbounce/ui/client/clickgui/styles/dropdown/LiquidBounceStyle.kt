/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI.accentColor
import net.minusmc.minusbounce.ui.client.clickgui.Panel
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.ButtonElement
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.ModuleElement
import net.minusmc.minusbounce.ui.client.clickgui.DropDownClickGui
import net.minusmc.minusbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.ui.font.GameFontRenderer
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.block.BlockUtils.getBlockName
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Mouse
import java.awt.Color
import net.minusmc.minusbounce.utils.MathUtils.round
import kotlin.math.max
import kotlin.math.min

class LiquidBounceStyle : DropDownClickGui("LiquidBounce") { // co đụng vô dau
    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel?) {
        RenderUtils.drawBorderedRect(panel!!.x.toFloat() - if (panel.scrollbar) 4 else 0, panel.y.toFloat(), panel.x.toFloat() + panel.width, panel.y.toFloat() + 19 + panel.getFade(), 1f, Color(255, 255, 255, 90).rgb, Int.MIN_VALUE)
        val textWidth = Fonts.font35.getStringWidth("§f" + StringUtils.stripControlCodes(panel.name)).toFloat()
        Fonts.font35.drawString("§f" + panel.name, (panel.x - (textWidth - 100.0f) / 2f).toInt(), panel.y + 7, -16777216)
        if (panel.scrollbar && panel.getFade() > 0) {
            RenderUtils.drawRect(panel.x - 1.5f, (panel.y + 21).toFloat(), panel.x - 0.5f, (panel.y + 16 + panel.getFade()).toFloat(), Int.MAX_VALUE)
            val maxElementsValue = MinusBounce.moduleManager[ClickGUI::class.java]!!.maxElementsValue.get()
            RenderUtils.drawRect((panel.x - 2).toFloat(),panel.y + 30 + (panel.getFade() - 24f) / (panel.elements.size - maxElementsValue) * panel.dragged - 10.0f, panel.x.toFloat(), panel.y + 40 + (panel.getFade() - 24.0f) / (panel.elements.size - maxElementsValue) * panel.dragged, Int.MIN_VALUE)
        }
    }

    override fun drawDescription(mouseX: Int, mouseY: Int, text: String?) {
        val textWidth = Fonts.font35.getStringWidth(text!!)
        RenderUtils.drawBorderedRect((mouseX + 9).toFloat(), mouseY.toFloat(), (mouseX + textWidth + 14).toFloat(), (mouseY + Fonts.font35.FONT_HEIGHT + 3).toFloat(), 1f, Color(255, 255, 255, 90).rgb, Int.MIN_VALUE)
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, mouseX + 12, mouseY + Fonts.font35.FONT_HEIGHT / 2, Int.MAX_VALUE)
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement?) {
        GlStateManager.resetColor()
        Fonts.font35.drawString(buttonElement!!.displayName, (buttonElement.x + 5), buttonElement.y + 7, buttonElement.color)
    }

    override fun drawValues(moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val moduleValues = moduleElement.module.values
        if (moduleValues.isNotEmpty()) {
            Fonts.font35.drawString("+", moduleElement.x + moduleElement.width - 8, moduleElement.y + moduleElement.height / 2, Color.WHITE.rgb)
            if (moduleElement.isShowSettings) {
                yPos = moduleElement.y + 4
                for (value in moduleValues) {
                    if (!value.canDisplay.invoke()) continue
                    val isNumber = value.get() is Number || value is IntRangeValue || value is FloatRangeValue
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
                if (moduleElement.settingsWidth > 0f && yPos > moduleElement.y + 4)
                    RenderUtils.drawBorderedRect((moduleElement.x + moduleElement.width + 4).toFloat(), (moduleElement.y + 6).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 2).toFloat(), 1f, Int.MIN_VALUE, 0)
            }
        }
    }

    override fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement?) {
        val guiColor = accentColor!!.rgb
        GlStateManager.resetColor()
        Fonts.font35.drawString(moduleElement!!.displayName, (moduleElement.x + 5), moduleElement.y + 7, if (moduleElement.module.state) guiColor else Int.MAX_VALUE)
        drawValues(moduleElement, mouseX, mouseY)
    }

    
    override fun drawBoolValue(value: BoolValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth), (yPos + 14).toFloat(), Int.MIN_VALUE)

        if ((mouseX >= moduleElement.x + moduleElement.width + 4) && (mouseX <= (moduleElement.x + moduleElement.width + moduleElement.settingsWidth)) && (mouseY >= yPos + 2) && (mouseY <= yPos + 14)) {
            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                value.set(!value.get())
                MinecraftInstance.mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, if (value.get()) guiColor else Int.MAX_VALUE)
        yPos += 12
    }

    override fun drawListValue(value: ListValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 16) moduleElement.settingsWidth = textWidth + 16
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 14).toFloat(), Int.MIN_VALUE)
        GlStateManager.resetColor()
        Fonts.font35.drawString("§c$text", moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        Fonts.font35.drawString(if (value.openList) "-" else "+", (moduleElement.x + moduleElement.width + moduleElement.settingsWidth - if (value.openList) 5 else 6).toInt(), yPos + 4, 0xffffff)
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                value.openList = !value.openList
                MinecraftInstance.mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
            }
        }
        yPos += 12
        for (valueOfList in value.values) {
            val textWidth2 = Fonts.font35.getStringWidth(">$valueOfList").toFloat()
            if (moduleElement.settingsWidth < textWidth2 + 8) moduleElement.settingsWidth = textWidth2 + 8
            if (value.openList) {
                RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 14).toFloat(), Int.MIN_VALUE )
                if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                    if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                        value.set(valueOfList)
                        MinecraftInstance.mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
                    }
                }
                GlStateManager.resetColor()
                Fonts.font35.drawString(">", moduleElement.x + moduleElement.width + 6, yPos + 4, Int.MAX_VALUE)
                Fonts.font35.drawString( valueOfList, moduleElement.x + moduleElement.width + 14, yPos + 4, if (value.get().equals(valueOfList, ignoreCase = true) ) guiColor else Int.MAX_VALUE)
                yPos += 12
            }
        }
    }

    override fun drawFloatValue(value: FloatValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c" + round(value.get()) + value.suffix
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Int.MIN_VALUE)
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 19).toFloat(), Int.MAX_VALUE)
        val sliderValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderValue, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), guiColor )
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4 && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
            val dWheel = Mouse.getDWheel()
            if (Mouse.hasWheel() && dWheel != 0) {
                if (dWheel > 0) value.set(min((value.get() + 0.01f).toDouble(), value.maximum.toDouble()))
                if (dWheel < 0) value.set(max((value.get() - 0.01f).toDouble(), value.minimum.toDouble()))
            }
            if (Mouse.isButtonDown(0)) {
                val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                value.set(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()))
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    override fun drawIntegerValue(value: IntegerValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c" + if (value is BlockValue) getBlockName(value.get()) + " (" + value.get() + ")" else value.get().toString() + value.suffix
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Int.MIN_VALUE)
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 19).toFloat(), Int.MAX_VALUE)
        val sliderValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderValue, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), guiColor)
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
            val dWheel = Mouse.getDWheel()
            if (Mouse.hasWheel() && dWheel != 0) {
                if (dWheel > 0) value.set(min((value.get() + 1).toDouble(), value.maximum.toDouble()))
                if (dWheel < 0) value.set(max((value.get() - 1).toDouble(), value.minimum.toDouble()))
            }
            if (Mouse.isButtonDown(0)) {
                val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                value.set((value.minimum + (value.maximum - value.minimum) * i).toInt())
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    override fun drawFontValue(value: FontValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val fontRenderer = value.get()
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 14).toFloat(), Int.MIN_VALUE)
        var displayString = "Font: Unknown"
        if (fontRenderer is GameFontRenderer) {
            displayString = "Font: " + fontRenderer.defaultFont.font.name + " - " + fontRenderer.defaultFont.font.size
        } else if (fontRenderer === Fonts.minecraftFont) displayString = "Font: Minecraft" else {
            val objects = Fonts.getFontDetails(fontRenderer)
            if (objects != null) {
                displayString = objects[0].toString() + if (objects[1] as Int != -1) " - " + objects[1] else ""
            }
        }
        Fonts.font35.drawString(displayString, moduleElement.x + moduleElement.width + 6, yPos + 4, Color.WHITE.rgb)
        val stringWidth = Fonts.font35.getStringWidth(displayString)
        if (moduleElement.settingsWidth < stringWidth + 8) moduleElement.settingsWidth = (stringWidth + 8).toFloat()
        if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) && mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 4 && mouseY <= yPos + 12) {
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
        val text = value.name + "§f: §c" + value.get()
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 14).toFloat(), Int.MIN_VALUE)
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 12
    }

    override fun drawIntRangeValue(value: IntRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c${value.get().getMin()} - ${value.get().getMax()}"
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Int.MIN_VALUE)
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 19).toFloat(), Int.MAX_VALUE)
        val sliderMinValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get().getMin() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderMinValue, (yPos + 15).toFloat(), sliderMinValue + 11, (yPos + 21).toFloat(), guiColor)
        val sliderMaxValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get().getMax() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderMaxValue, (yPos + 15).toFloat(), sliderMaxValue + 11, (yPos + 21).toFloat(), guiColor)
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
        val text = value.name + "§f: §c${round(value.get().getMin())}${value.suffix} - ${round(value.get().getMax())}${value.suffix}"
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Int.MIN_VALUE)
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 19).toFloat(), Int.MAX_VALUE)
        val sliderMinValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get().getMin() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderMinValue, (yPos + 15).toFloat(), sliderMinValue + 10, (yPos + 21).toFloat(), guiColor)
        val sliderMaxValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get().getMax() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderMaxValue, (yPos + 15).toFloat(), sliderMaxValue + 11, (yPos + 21).toFloat(), guiColor)
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
}
