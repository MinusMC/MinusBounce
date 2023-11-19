/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.style.styles

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI.accentColor
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
import kotlin.math.max
import kotlin.math.min

class NullStyle : Style() {
    private fun modifyAlpha(col: Color?, alpha: Int): Color {
        return Color(col!!.red, col.green, col.blue, alpha)
    }

    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel?) {
        RenderUtils.drawGradientSideways(
            panel!!.x.toFloat() - 3,
            panel.y.toFloat(),
            panel.x.toFloat() + panel.width + 3,
            panel.y.toFloat() + 19,
            modifyAlpha(
                accentColor, 120
            ).rgb,
            modifyAlpha(
                accentColor!!.darker().darker(), 120
            ).rgb
        )
        GlStateManager.resetColor()
        if (panel.getFade() > 0) RenderUtils.drawRect(
            panel.x.toFloat(),
            panel.y.toFloat() + 19,
            panel.x.toFloat() + panel.width,
            (panel.y + 19 + panel.getFade()).toFloat(),
            Int.MIN_VALUE
        )
        GlStateManager.resetColor()
        val textWidth = Fonts.font35.getStringWidth(
            "§f" + StringUtils.stripControlCodes(
                panel.name
            )
        ).toFloat()
        Fonts.font35.drawString(
            "§f" + panel.name,
            (panel.x - (textWidth - 100.0f) / 2f).toInt(),
            panel.y + 7,
            Int.MAX_VALUE
        )
    }

    override fun drawDescription(mouseX: Int, mouseY: Int, text: String?) {
        val textWidth = Fonts.font35.getStringWidth(text!!)
        RenderUtils.drawRect(
            (mouseX + 9).toFloat(),
            mouseY.toFloat(),
            (mouseX + textWidth + 14).toFloat(),
            (mouseY + Fonts.font35.FONT_HEIGHT + 3).toFloat(),
            modifyAlpha(
                accentColor, 120
            ).rgb
        )
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, mouseX + 12, mouseY + Fonts.font35.FONT_HEIGHT / 2, Int.MAX_VALUE)
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement?) {
        GlStateManager.resetColor()
        Fonts.font35.drawString(
            buttonElement!!.displayName,
            (buttonElement.x + 5),
            buttonElement.y + 7,
            buttonElement.color
        )
    }

    override fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement?) {
        val guiColor = accentColor!!.rgb
        GlStateManager.resetColor()
        Fonts.font35.drawString(
            moduleElement!!.displayName,
            (moduleElement.x + 5),
            moduleElement.y + 7,
            if (moduleElement.module.state) guiColor else Int.MAX_VALUE
        )
        val moduleValues = moduleElement.module.values
        if (moduleValues.isNotEmpty()) {
            Fonts.font35.drawString(
                "+", moduleElement.x + moduleElement.width - 10,
                moduleElement.y + moduleElement.height / 2, Color.WHITE.rgb
            )
            if (moduleElement.isShowSettings) {
                var yPos = moduleElement.y + 4
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
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 14).toFloat(),
                            Int.MIN_VALUE
                        )
                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                                value.set(!value.get())
                                mc.soundHandler.playSound(
                                    PositionedSoundRecord.create(
                                        ResourceLocation("gui.button.press"),
                                        1.0f
                                    )
                                )
                            }
                        }
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(
                            text,
                            moduleElement.x + moduleElement.width + 6,
                            yPos + 4,
                            if (value.get()) guiColor else Int.MAX_VALUE
                        )
                        yPos += 12
                    } else if (value is ListValue) {
                        val text = value.name
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (moduleElement.settingsWidth < textWidth + 16) moduleElement.settingsWidth = textWidth + 16
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 14).toFloat(),
                            Int.MIN_VALUE
                        )
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(
                            "§c$text",
                            moduleElement.x + moduleElement.width + 6,
                            yPos + 4,
                            0xffffff
                        )
                        Fonts.font35.drawString(
                            if (value.openList) "-" else "+",
                            (moduleElement.x + moduleElement.width + moduleElement.settingsWidth - if (value.openList) 5 else 6).toInt(),
                            yPos + 4,
                            0xffffff
                        )
                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                                value.openList = !value.openList
                                mc.soundHandler.playSound(
                                    PositionedSoundRecord.create(
                                        ResourceLocation("gui.button.press"),
                                        1.0f
                                    )
                                )
                            }
                        }
                        yPos += 12
                        for (valueOfList in value.values) {
                            val textWidth2 = Fonts.font35.getStringWidth(">$valueOfList").toFloat()
                            if (moduleElement.settingsWidth < textWidth2 + 12) moduleElement.settingsWidth =
                                textWidth2 + 12
                            if (value.openList) {
                                RenderUtils.drawRect(
                                    (moduleElement.x + moduleElement.width + 4).toFloat(),
                                    (yPos + 2).toFloat(),
                                    moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                                    (yPos + 14).toFloat(),
                                    Int.MIN_VALUE
                                )
                                if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                                    if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                                        value.set(valueOfList)
                                        mc.soundHandler.playSound(
                                            PositionedSoundRecord.create(
                                                ResourceLocation("gui.button.press"),
                                                1.0f
                                            )
                                        )
                                    }
                                }
                                GlStateManager.resetColor()
                                Fonts.font35.drawString(
                                    ">",
                                    moduleElement.x + moduleElement.width + 6,
                                    yPos + 4,
                                    Int.MAX_VALUE
                                )
                                Fonts.font35.drawString(
                                    valueOfList,
                                    moduleElement.x + moduleElement.width + 14,
                                    yPos + 4,
                                    if (value.get().equals(valueOfList, ignoreCase = true)
                                    ) guiColor else Int.MAX_VALUE
                                )
                                yPos += 12
                            }
                        }
                    } else if (value is FloatValue) {
                        val text = value.name + "§f: §c" + round(value.get()) + value.suffix
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 24).toFloat(),
                            Int.MIN_VALUE
                        )
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 8).toFloat(),
                            (yPos + 18).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4,
                            (yPos + 19).toFloat(),
                            Int.MAX_VALUE
                        )
                        val sliderValue =
                            moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum)
                        RenderUtils.drawRect(
                            8 + sliderValue,
                            (yPos + 15).toFloat(),
                            sliderValue + 11,
                            (yPos + 21).toFloat(),
                            guiColor
                        )
                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4 && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
                            val dWheel = Mouse.getDWheel()
                            if (Mouse.hasWheel() && dWheel != 0) {
                                if (dWheel > 0) value.set(
                                    min(
                                        (value.get() + 0.01f).toDouble(),
                                        value.maximum.toDouble()
                                    )
                                )
                                if (dWheel < 0) value.set(
                                    max(
                                        (value.get() - 0.01f).toDouble(),
                                        value.minimum.toDouble()
                                    )
                                )
                            }
                            if (Mouse.isButtonDown(0)) {
                                val i = MathHelper.clamp_double(
                                    ((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(),
                                    0.0,
                                    1.0
                                )
                                value.set(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()).toFloat())
                            }
                        }
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
                        yPos += 22
                    } else if (value is IntegerValue) {
                        val text =
                            value.name + "§f: §c" + if (value is BlockValue) getBlockName(value.get()) + " (" + value.get() + ")" else value.get()
                                .toString() + value.suffix
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 24).toFloat(),
                            Int.MIN_VALUE
                        )
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 8).toFloat(),
                            (yPos + 18).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4,
                            (yPos + 19).toFloat(),
                            Int.MAX_VALUE
                        )
                        val sliderValue =
                            moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum)
                        RenderUtils.drawRect(
                            8 + sliderValue,
                            (yPos + 15).toFloat(),
                            sliderValue + 11,
                            (yPos + 21).toFloat(),
                            guiColor
                        )
                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
                            val dWheel = Mouse.getDWheel()
                            if (Mouse.hasWheel() && dWheel != 0) {
                                if (dWheel > 0) value.set(
                                    min(
                                        (value.get() + 1).toDouble(),
                                        value.maximum.toDouble()
                                    )
                                )
                                if (dWheel < 0) value.set(
                                    max(
                                        (value.get() - 1).toDouble(),
                                        value.minimum.toDouble()
                                    )
                                )
                            }
                            if (Mouse.isButtonDown(0)) {
                                val i = MathHelper.clamp_double(
                                    ((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(),
                                    0.0,
                                    1.0
                                )
                                value.set((value.minimum + (value.maximum - value.minimum) * i).toInt())
                            }
                        }
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
                        yPos += 22
                    } else if (value is FontValue) {
                        val fontRenderer = value.get()
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 14).toFloat(),
                            Int.MIN_VALUE
                        )
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
                            yPos + 4,
                            Color.WHITE.rgb
                        )
                        val stringWidth = Fonts.font35.getStringWidth(displayString)
                        if (moduleElement.settingsWidth < stringWidth + 8) moduleElement.settingsWidth =
                            (stringWidth + 8).toFloat()
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
                    } else {
                        val text = value.name + "§f: §c" + value.get()
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 14).toFloat(),
                            Int.MIN_VALUE
                        )
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
                        yPos += 12
                    }
                    if (isNumber) {
                        // This state is cleaned up in ClickGUI
                        assumeNonVolatile = true
                    }
                }
                moduleElement.updatePressed()
                mouseDown = Mouse.isButtonDown(0)
                rightMouseDown = Mouse.isButtonDown(1)
                if (moduleElement.settingsWidth > 0f && yPos > moduleElement.y + 4) RenderUtils.drawBorderedRect(
                    (moduleElement.x + moduleElement.width + 4).toFloat(),
                    (moduleElement.y + 6).toFloat(),
                    moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                    (yPos + 2).toFloat(),
                    1f,
                    Int.MIN_VALUE,
                    0
                )
            }
        }
    }
}
