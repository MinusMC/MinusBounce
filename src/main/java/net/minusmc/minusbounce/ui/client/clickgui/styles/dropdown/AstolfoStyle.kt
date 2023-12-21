package net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI.accentColor
import net.minusmc.minusbounce.ui.client.clickgui.Panel
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.ButtonElement
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.ModuleElement
import net.minusmc.minusbounce.ui.client.clickgui.DropDownClickGui
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.ui.font.GameFontRenderer
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.block.BlockUtils.getBlockName
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Mouse
import java.awt.Color
import java.util.*
import kotlin.math.min
import kotlin.math.max
import net.minusmc.minusbounce.utils.MathUtils.round

class AstolfoStyle: DropDownClickGui("Astolfo") {
    private fun getCategoryColor(categoryName: String): Color? {
        return when (categoryName.lowercase()) {
            "combat" -> Color(231, 75, 58, 175)
            "player" -> Color(142, 69, 174, 175)
            "movement" -> Color(46, 205, 111, 175)
            "render" -> Color(76, 143, 200, 175)
            "world" -> Color(233, 215, 100, 175)
            "misc" -> Color(244, 157, 19, 175)
            else -> accentColor
        }
    }

    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel?) {
        RenderUtils.drawRect(panel!!.x.toFloat() - 3, panel.y.toFloat() - 1, panel.x.toFloat() + panel.width + 3,(panel.y + 22 + panel.getFade()).toFloat(), getCategoryColor(panel.name)!!.rgb)
        RenderUtils.drawRect((panel.x - 2).toFloat(), panel.y.toFloat(), (panel.x + panel.width + 2).toFloat(), (panel.y + 21 + panel.getFade()).toFloat(),Color(17, 17, 17).rgb)
        RenderUtils.drawRect(panel.x.toFloat() + 1, panel.y.toFloat() + 19, panel.x.toFloat() + panel.width - 1,(panel.y + 18 + panel.getFade()).toFloat(), Color(26, 26, 26).rgb)
        GlStateManager.resetColor()
        Fonts.minecraftFont.drawString("§l" + panel.name.lowercase(Locale.getDefault()),panel.x + 2,panel.y + 6,Int.MAX_VALUE)
    }

    override fun drawDescription(mouseX: Int, mouseY: Int, text: String?) {
        val textWidth = Fonts.minecraftFont.getStringWidth(text)
        RenderUtils.drawRect((mouseX + 9).toFloat(),mouseY.toFloat(),(mouseX + textWidth + 14).toFloat(),(mouseY + Fonts.minecraftFont.FONT_HEIGHT + 3).toFloat(),Color(26, 26, 26).rgb)
        GlStateManager.resetColor()
        Fonts.minecraftFont.drawString(text!!.lowercase(Locale.getDefault()), mouseX + 12,mouseY + Fonts.minecraftFont.FONT_HEIGHT / 2, Int.MAX_VALUE)
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement?) {
        Gui.drawRect(buttonElement!!.x - 1, buttonElement.y + 1, buttonElement.x + buttonElement.width + 1, buttonElement.y + buttonElement.height + 2, ColorUtils.hoverColor(if (buttonElement.color != Int.MAX_VALUE) accentColor else Color(26, 26, 26), buttonElement.hoverTime).rgb)
        GlStateManager.resetColor()
        Fonts.minecraftFont.drawString(buttonElement.displayName!!.lowercase(), buttonElement.x + 3, buttonElement.y + 6, Color.WHITE.rgb)
    }

    override fun drawValues(moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val moduleValues = moduleElement.module.values
        if (moduleValues.isNotEmpty()) {
            Fonts.minecraftFont.drawString("+", moduleElement.x + moduleElement.width - 8, moduleElement.y + moduleElement.height / 2, Color(255, 255, 255, 200).rgb)
            if (moduleElement.isShowSettings) {
                yPos = moduleElement.y + 4
                for (value in moduleValues) {
                    if (!value.canDisplay.invoke()) continue
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
                }
                moduleElement.updatePressed()
                mouseDown = Mouse.isButtonDown(0)
                rightMouseDown = Mouse.isButtonDown(1)
                if (moduleElement.settingsWidth > 0f && yPos > moduleElement.y + 4)
                RenderUtils.drawBorderedRect((moduleElement.x + moduleElement.width + 4).toFloat(), (moduleElement.y + 6).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth), (yPos + 2).toFloat(), 1f, Color(26, 26, 26).rgb, 0)
            }
        }
    }

    override fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement?) {
        Gui.drawRect(moduleElement!!.x + 1, moduleElement.y + 1, moduleElement.x + moduleElement.width - 1, moduleElement.y + moduleElement.height + 2, ColorUtils.hoverColor(Color(26, 26, 26), moduleElement.hoverTime).rgb)
        val categoryColor = getCategoryColor(moduleElement.module.category.name)!!
        Gui.drawRect(moduleElement.x + 1, moduleElement.y + 1, moduleElement.x + moduleElement.width - 1, moduleElement.y + moduleElement.height + 2, ColorUtils.hoverColor(Color(categoryColor.red, categoryColor.green, categoryColor.blue, moduleElement.slowlyFade), moduleElement.hoverTime).rgb)
        GlStateManager.resetColor()
        Fonts.minecraftFont.drawString(moduleElement.displayName!!.lowercase(), moduleElement.x + 3, moduleElement.y + 7, Int.MAX_VALUE)
        drawValues(moduleElement, mouseX, mouseY)
    }

    override fun drawBoolValue(value: BoolValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name
        val textWidth = Fonts.minecraftFont.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth), (yPos + 14).toFloat(), Color(26, 26, 26).rgb)

        if ((mouseX >= moduleElement.x + moduleElement.width + 4) && (mouseX <= (moduleElement.x + moduleElement.width + moduleElement.settingsWidth)) && (mouseY >= yPos + 2) && (mouseY <= yPos + 14)) {
            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                value.set(!value.get())
                MinecraftInstance.mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
            }
        }
        GlStateManager.resetColor()
        Fonts.minecraftFont.drawString(text.lowercase(), moduleElement.x + moduleElement.width + 6, yPos + 4, if (value.get()) guiColor else Int.MAX_VALUE)
        yPos += 12
    }

    override fun drawListValue(value: ListValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name
        val textWidth = Fonts.minecraftFont.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 16) moduleElement.settingsWidth = textWidth + 16
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth), (yPos + 14).toFloat(), Color(26, 26, 26).rgb)

        GlStateManager.resetColor()
        Fonts.minecraftFont.drawString("§c${text.lowercase()}", moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        Fonts.minecraftFont.drawString(if (value.openList) "-" else "+", ((moduleElement.x + moduleElement.width + moduleElement.settingsWidth) - (if (value.openList) 5 else 6)).toInt(), yPos + 4, 0xffffff)
        if ((mouseX >= moduleElement.x + moduleElement.width + 4) && (mouseX <= (moduleElement.x + moduleElement.width + moduleElement.settingsWidth)) && (mouseY >= yPos + 2) && (mouseY <= yPos + 14)) {
            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                value.openList = !value.openList
                MinecraftInstance.mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
            }
        }
        yPos += 12
        for (valueOfList in value.values) {
            val textWidth2 = Fonts.minecraftFont.getStringWidth(">$valueOfList").toFloat()
            if (moduleElement.settingsWidth < textWidth2 + 12) moduleElement.settingsWidth = textWidth2 + 12
            if (value.openList) {
                RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth), (yPos + 14).toFloat(), Color(26, 26, 26).rgb)
                if ((mouseX >= moduleElement.x + moduleElement.width + 4) && (mouseX <= (moduleElement.x + moduleElement.width + moduleElement.settingsWidth)) && (mouseY >= yPos + 2) && (mouseY <= yPos + 14)) {
                    if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                        value.set(valueOfList)
                        MinecraftInstance.mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
                    }
                }
                GlStateManager.resetColor()
                Fonts.minecraftFont.drawString(">", (moduleElement.x + moduleElement.width + 6), yPos + 4, Int.MAX_VALUE)
                Fonts.minecraftFont.drawString(valueOfList.lowercase(), moduleElement.x + moduleElement.width + 14, yPos + 4,if (value.get().equals(valueOfList, true)) guiColor else Int.MAX_VALUE)
                yPos += 12
            }
        }
    }

    override fun drawFloatValue(value: FloatValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c" + round(value.get())
        val textWidth = Fonts.minecraftFont.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Color(26, 26, 26).rgb)
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 19).toFloat(), Int.MAX_VALUE)
        val sliderValue = moduleElement.x + moduleElement.width + ((moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum))
        RenderUtils.drawRect(sliderValue + 8, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), guiColor)
        if ((mouseX >= moduleElement.x + moduleElement.width + 4) && (mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4) && (mouseY >= yPos + 15) && (mouseY <= yPos + 21)) {
            if (Mouse.isButtonDown(0)) {
                val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                value.set(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()))
            }
        }
        GlStateManager.resetColor()
        Fonts.minecraftFont.drawString(text.lowercase(), moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    override fun drawIntegerValue(value: IntegerValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c" + (if (value is BlockValue) getBlockName(value.get()) + " (${value.get()})" else value.get())
        val textWidth = Fonts.minecraftFont.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth), (yPos + 24).toFloat(), Color(26, 26, 26).rgb)
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth) - 4, (yPos + 19).toFloat(), Int.MAX_VALUE)
        val sliderValue = moduleElement.x + moduleElement.width + ((moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum))
        RenderUtils.drawRect(sliderValue + 8, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), guiColor)
        if ((mouseX >= moduleElement.x + moduleElement.width + 4) && (mouseX <= (moduleElement.x + moduleElement.width + moduleElement.settingsWidth)) && (mouseY >= yPos + 15) && (mouseY <= yPos + 21)) {
            if (Mouse.isButtonDown(0)) {
                val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                value.set((value.minimum + (value.maximum - value.minimum) * i).toInt())
            }
        }
        GlStateManager.resetColor()
        Fonts.minecraftFont.drawString(text.lowercase(), moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    override fun drawFontValue(value: FontValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val fontRenderer = value.get()
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth), (yPos + 14).toFloat(), Color(26, 26, 26).rgb)
        var displayString = "Font: Unknown"
        if (fontRenderer is GameFontRenderer) {
            displayString = "Font: " + fontRenderer.defaultFont.font.name + " - " + fontRenderer.defaultFont.font.size
        } else if (fontRenderer === Fonts.minecraftFont) displayString = "Font: minecraftFont" else {
            val objects = Fonts.getFontDetails(fontRenderer)
            if (objects != null) {
                displayString = objects[0].toString() + (if (objects[1] as Int != -1) " - " + objects[1] else "")
            }
        }
        Fonts.minecraftFont.drawString(displayString.lowercase(), moduleElement.x + moduleElement.width + 6, yPos + 4, Color.WHITE.rgb)
        val stringWidth = Fonts.minecraftFont.getStringWidth(displayString)
        if (moduleElement.settingsWidth < stringWidth + 8) moduleElement.settingsWidth = (stringWidth + 8).toFloat()
        if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) && (mouseX >= (moduleElement.x + moduleElement.width + 4)) && (mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth) && (mouseY >= yPos + 4) && (mouseY <= yPos + 12)) {
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
        val textWidth = Fonts.minecraftFont.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth), (yPos + 14).toFloat(), Color(26, 26, 26).rgb)
        GlStateManager.resetColor()
        Fonts.minecraftFont.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 12
    }

    override fun drawIntRangeValue(value: IntRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c${value.get().getMin()} - ${value.get().getMax()}"
        val textWidth = Fonts.minecraftFont.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Color(26, 26, 26).rgb)
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
        Fonts.minecraftFont.drawString(text.lowercase(), moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    override fun drawFloatRangeValue(value: FloatRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c${round(value.get().getMin())}${value.suffix} - ${round(value.get().getMax())}${value.suffix}"
        val textWidth = Fonts.minecraftFont.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Color(26, 26, 26).rgb)
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
        Fonts.minecraftFont.drawString(text.lowercase(), moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

}
