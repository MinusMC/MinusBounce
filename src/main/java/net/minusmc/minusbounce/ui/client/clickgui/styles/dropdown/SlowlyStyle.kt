/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.style.styles

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import net.minusmc.minusbounce.ui.client.clickgui.Panel
import net.minusmc.minusbounce.ui.client.clickgui.elements.ButtonElement
import net.minusmc.minusbounce.ui.client.clickgui.elements.ModuleElement
import net.minusmc.minusbounce.ui.client.clickgui.style.Style
import net.minusmc.minusbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.ui.font.GameFontRenderer
import net.minusmc.minusbounce.utils.block.BlockUtils.getBlockName
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Mouse
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min

class SlowlyStyle : Style() {
    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel?) {
        RenderUtils.drawBorderedRect(
            panel!!.x.toFloat(),
            panel.y.toFloat() - 3,
            panel.x.toFloat() + panel.width,
            panel.y.toFloat() + 17,
            3f,
            Color(0, 0, 0).rgb,
            Color(0, 0, 0).rgb
        )
        if (panel.getFade() > 0) {
            RenderUtils.drawBorderedRect(
                panel.x.toFloat(),
                panel.y.toFloat() + 17,
                panel.x.toFloat() + panel.width,
                (panel.y + 19 + panel.getFade()).toFloat(),
                3f,
                Color(0, 0, 0).rgb,
                Color(0, 0, 0).rgb
            )
            RenderUtils.drawBorderedRect(
                panel.x.toFloat(),
                (panel.y + 17 + panel.getFade()).toFloat(),
                panel.x.toFloat() + panel.width,
                (panel.y + 19 + panel.getFade() + 5).toFloat(),
                3f,
                Color(0, 0, 0).rgb,
                Color(0, 0, 0).rgb
            )
        }
        GlStateManager.resetColor()
        val textWidth = Fonts.font35.getStringWidth(
            "§f" + StringUtils.stripControlCodes(
                panel.name
            )
        ).toFloat()
        Fonts.font35.drawString(
            panel.name,
            (panel.x - (textWidth - 100.0f) / 2f).toInt(),
            panel.y + 7 - 3,
            Color.WHITE.rgb
        )
    }

    override fun drawDescription(mouseX: Int, mouseY: Int, text: String?) {
        val textWidth = Fonts.font35.getStringWidth(text!!)
        RenderUtils.drawBorderedRect(
            (mouseX + 9).toFloat(),
            mouseY.toFloat(),
            (mouseX + textWidth + 14).toFloat(),
            (mouseY + Fonts.font35.FONT_HEIGHT + 3).toFloat(),
            3f,
            Color(0, 0, 0).rgb,
            Color(0, 0, 0).rgb
        )
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, mouseX + 12, mouseY + Fonts.font35.FONT_HEIGHT / 2, Color.WHITE.rgb)
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement?) {
        Gui.drawRect(
            buttonElement!!.x - 1,
            buttonElement.y - 1,
            buttonElement.x + buttonElement.width + 1,
            buttonElement.y + buttonElement.height + 1,
            hoverColor(
                if (buttonElement.color != Int.MAX_VALUE) Color(10, 10, 10) else Color(0, 0, 0),
                buttonElement.hoverTime
            ).rgb
        )
        GlStateManager.resetColor()
        Fonts.font35.drawString(buttonElement.displayName, buttonElement.x + 5, buttonElement.y + 7, Color.WHITE.rgb)
    }

    override fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement?) {
        Gui.drawRect(
            moduleElement!!.x - 1,
            moduleElement.y - 1,
            moduleElement.x + moduleElement.width + 1,
            moduleElement.y + moduleElement.height + 1,
            hoverColor(
                Color(40, 40, 40), moduleElement.hoverTime
            ).rgb
        )
        Gui.drawRect(
            moduleElement.x - 1,
            moduleElement.y - 1,
            moduleElement.x + moduleElement.width + 1,
            moduleElement.y + moduleElement.height + 1,
            hoverColor(
                Color(0, 0, 0, moduleElement.slowlyFade), moduleElement.hoverTime
            ).rgb
        )
        GlStateManager.resetColor()
        Fonts.font35.drawString(moduleElement.displayName, moduleElement.x + 5, moduleElement.y + 7, Color.WHITE.rgb)

        // Draw settings
        val moduleValues = moduleElement.module.values
        if (moduleValues.isNotEmpty()) {
            Fonts.font35.drawString(
                ">",
                moduleElement.x + moduleElement.width - 8,
                moduleElement.y + 5,
                Color.WHITE.rgb
            )
            if (moduleElement.isShowSettings) {
                if (moduleElement.settingsWidth > 0f && moduleElement.slowlySettingsYPos > moduleElement.y + 6) RenderUtils.drawBorderedRect(
                    (moduleElement.x + moduleElement.width + 4).toFloat(),
                    (moduleElement.y + 6).toFloat(),
                    moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                    (moduleElement.slowlySettingsYPos + 2).toFloat(),
                    3f,
                    Color(0, 0, 0).rgb,
                    Color(0, 0, 0).rgb
                )
                moduleElement.slowlySettingsYPos = moduleElement.y + 6
                for (value in moduleValues) {
                    if (!value.canDisplay.invoke()) continue
                    val isNumber = value.get() is Number
                    if (isNumber) {
                        assumeNonVolatile = false
                    }
                    if (value is BoolValue) {
                        val text = value.name
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= moduleElement.slowlySettingsYPos && mouseY <= moduleElement.slowlySettingsYPos + 12 && Mouse.isButtonDown(
                                0
                            ) && moduleElement.isntPressed()
                        ) {
                            value.set(!value.get())
                            mc.soundHandler.playSound(
                                PositionedSoundRecord.create(
                                    ResourceLocation("gui.button.press"),
                                    1.0f
                                )
                            )
                        }
                        Fonts.font35.drawString(
                            text,
                            moduleElement.x + moduleElement.width + 6,
                            moduleElement.slowlySettingsYPos + 2,
                            if (value.get()) Color.WHITE.rgb else Int.MAX_VALUE
                        )
                        moduleElement.slowlySettingsYPos += 11
                    } else if (value is ListValue) {
                        val text = value.name
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (moduleElement.settingsWidth < textWidth + 16) moduleElement.settingsWidth = textWidth + 16
                        Fonts.font35.drawString(
                            text,
                            moduleElement.x + moduleElement.width + 6,
                            moduleElement.slowlySettingsYPos + 2,
                            0xffffff
                        )
                        Fonts.font35.drawString(
                            if (value.openList) "-" else "+",
                            (moduleElement.x + moduleElement.width + moduleElement.settingsWidth - if (value.openList) 5 else 6).toInt(),
                            moduleElement.slowlySettingsYPos + 2,
                            0xffffff
                        )
                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= moduleElement.slowlySettingsYPos && mouseY <= moduleElement.slowlySettingsYPos + Fonts.font35.FONT_HEIGHT && Mouse.isButtonDown(
                                0
                            ) && moduleElement.isntPressed()
                        ) {
                            value.openList = !value.openList
                            mc.soundHandler.playSound(
                                PositionedSoundRecord.create(
                                    ResourceLocation("gui.button.press"),
                                    1.0f
                                )
                            )
                        }
                        moduleElement.slowlySettingsYPos += Fonts.font35.FONT_HEIGHT + 1
                        for (valueOfList in value.values) {
                            val textWidth2 = Fonts.font35.getStringWidth("> $valueOfList").toFloat()
                            if (moduleElement.settingsWidth < textWidth2 + 12) moduleElement.settingsWidth =
                                textWidth2 + 12
                            if (value.openList) {
                                if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= moduleElement.slowlySettingsYPos + 2 && mouseY <= moduleElement.slowlySettingsYPos + 14 && Mouse.isButtonDown(
                                        0
                                    ) && moduleElement.isntPressed()
                                ) {
                                    value.set(valueOfList)
                                    mc.soundHandler.playSound(
                                        PositionedSoundRecord.create(
                                            ResourceLocation("gui.button.press"),
                                            1.0f
                                        )
                                    )
                                }
                                GlStateManager.resetColor()
                                Fonts.font35.drawString(
                                    "> $valueOfList",
                                    moduleElement.x + moduleElement.width + 6,
                                    moduleElement.slowlySettingsYPos + 2,
                                    if (value.get().equals(valueOfList, ignoreCase = true)
                                    ) Color.WHITE.rgb else Int.MAX_VALUE
                                )
                                moduleElement.slowlySettingsYPos += Fonts.font35.FONT_HEIGHT + 1
                            }
                        }
                        if (!value.openList) {
                            moduleElement.slowlySettingsYPos += 1
                        }
                    } else if (value is FloatValue) {
                        val text = value.name + "§f: " + round(value.get()) + value.suffix
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
                        val valueOfSlide = drawSlider(
                            value.get(),
                            value.minimum,
                            value.maximum,
                            false,
                            moduleElement.x + moduleElement.width + 8,
                            moduleElement.slowlySettingsYPos + 14,
                            moduleElement.settingsWidth.toInt() - 12,
                            mouseX,
                            mouseY,
                            Color(120, 120, 120)
                        )
                        if (valueOfSlide != value.get()) value.set(valueOfSlide)
                        Fonts.font35.drawString(
                            text,
                            moduleElement.x + moduleElement.width + 6,
                            moduleElement.slowlySettingsYPos + 3,
                            0xffffff
                        )
                        moduleElement.slowlySettingsYPos += 19
                    } else if (value is IntegerValue) {
                        val text =
                            value.name + "§f: " + if (value is BlockValue) getBlockName(value.get()) + " (" + value.get() + ")" else value.get()
                                .toString() + value.suffix
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
                        val valueOfSlide = drawSlider(
                            value.get().toFloat(),
                            value.minimum.toFloat(),
                            value.maximum.toFloat(),
                            true,
                            moduleElement.x + moduleElement.width + 8,
                            moduleElement.slowlySettingsYPos + 14,
                            moduleElement.settingsWidth.toInt() - 12,
                            mouseX,
                            mouseY,
                            Color(120, 120, 120)
                        )
                        if (valueOfSlide != value.get().toFloat()) value.set(valueOfSlide.toInt())
                        Fonts.font35.drawString(
                            text,
                            moduleElement.x + moduleElement.width + 6,
                            moduleElement.slowlySettingsYPos + 3,
                            0xffffff
                        )
                        moduleElement.slowlySettingsYPos += 19
                    } else if (value is FontValue) {
                        val fontRenderer = value.get()
                        var displayString = "Font: Unknown"
                        if (fontRenderer is GameFontRenderer) {
                            displayString =
                                "Font: " + fontRenderer.defaultFont.font.name + " - " + fontRenderer.defaultFont.font.size
                        } else if (fontRenderer === Fonts.minecraftFont) displayString = "Font: Minecraft" else {
                            val objects = Fonts.getFontDetails(fontRenderer)
                            if (objects != null) {
                                displayString =
                                    objects[0].toString() + if (objects[1] as Int != -1) " - " + objects[1] else ""
                            }
                        }
                        Fonts.font35.drawString(
                            displayString,
                            moduleElement.x + moduleElement.width + 6,
                            moduleElement.slowlySettingsYPos + 2,
                            Color.WHITE.rgb
                        )
                        val stringWidth = Fonts.font35.getStringWidth(displayString)
                        if (moduleElement.settingsWidth < stringWidth + 8) moduleElement.settingsWidth =
                            (stringWidth + 8).toFloat()
                        if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) && mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= moduleElement.slowlySettingsYPos && mouseY <= moduleElement.slowlySettingsYPos + 12) {
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
                        moduleElement.slowlySettingsYPos += 11
                    } else {
                        val text = value.name + "§f: " + value.get()
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(
                            text,
                            moduleElement.x + moduleElement.width + 6,
                            moduleElement.slowlySettingsYPos + 4,
                            0xffffff
                        )
                        moduleElement.slowlySettingsYPos += 12
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

    companion object {
        fun drawSlider(
            value: Float,
            min: Float,
            max: Float,
            inte: Boolean,
            x: Int,
            y: Int,
            width: Int,
            mouseX: Int,
            mouseY: Int,
            color: Color?
        ): Float {
            val displayValue =
                max(min.toDouble(), min(value.toDouble(), max.toDouble())).toFloat()
            val sliderValue = x.toFloat() + width.toFloat() * (displayValue - min) / (max - min)
            RenderUtils.drawRect(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + 2).toFloat(), Int.MAX_VALUE)
            RenderUtils.drawRect(x.toFloat(), y.toFloat(), sliderValue, (y + 2).toFloat(), color!!)
            RenderUtils.drawFilledCircle(sliderValue.toInt(), y + 1, 3f, color)
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 3) {
                val dWheel = Mouse.getDWheel()
                if (Mouse.hasWheel() && dWheel != 0) {
                    if (dWheel > 0) return min((value + if (inte) 1f else 0.01f).toDouble(), max.toDouble())
                        .toFloat()
                    if (dWheel < 0) return max((value - if (inte) 1f else 0.01f).toDouble(), min.toDouble())
                        .toFloat()
                }
                if (Mouse.isButtonDown(0)) {
                    val i =
                        MathHelper.clamp_double((mouseX.toDouble() - x.toDouble()) / (width.toDouble() - 3), 0.0, 1.0)
                    var bigDecimal = BigDecimal((min + (max - min) * i).toString())
                    bigDecimal = bigDecimal.setScale(2, 4)
                    return bigDecimal.toFloat()
                }
            }
            return value
        }
    }
}
