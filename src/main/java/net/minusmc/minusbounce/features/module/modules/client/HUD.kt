/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.client

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.AnimationUtils
import net.minusmc.minusbounce.utils.extensions.setAlpha
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.*

import java.awt.Color
import java.io.Serializable

@ModuleInfo(name = "HUD", description = "Toggles visibility of the HUD.", category = ModuleCategory.CLIENT, array = false)
object HUD : Module() {
    val themeMode = ListValue("ThemeStyle",  arrayOf("Cherry", "Water", "Magic", "DarkNight", "Sun", "Tree", "Flower", "Loyoi", "Soniga", "May", "Mint", "Cero", "Azure", "Custom"), "Cherry")
    private val redValue = IntegerValue("Red", 255, 0, 255) {
        themeMode.get().equals("custom", true)
    }
    private val greenValue = IntegerValue("Green", 255, 0, 255) {
        themeMode.get().equals("custom", true)
    }
    private val blueValue = IntegerValue("Blue", 255, 0, 255) {
        themeMode.get().equals("custom", true)
    }
    val rainbowStartValue = FloatValue("RainbowStart", 0.55f, 0f, 1f).displayable { false }
    val rainbowStopValue = FloatValue("RainbowStop", 0.85f, 0f, 1f).displayable { false }
    val rainbowSaturationValue = FloatValue("RainbowSaturation", 0.45f, 0f, 1f).displayable { false }
    val rainbowBrightnessValue = FloatValue("RainbowBrightness", 0.85f, 0f, 1f).displayable { false }
    val rainbowSpeedValue = IntegerValue("RainbowSpeed", 1500, 500, 7000).displayable { false }
    val fadespeed = IntegerValue("Fade-speed", 1, 1, 10).displayable { false }
    val updown = BoolValue(
        "Fade-Type",
        false
    ).displayable { false }
    val tabHead = BoolValue("Tab-HeadOverlay", true)
    val animHotbarValue = BoolValue("AnimatedHotbar", true)
    val blackHotbarValue = BoolValue("BlackHotbar", true)
    val inventoryParticle = BoolValue("InventoryParticle", false)
    val fontChatValue = BoolValue("FontChat", false)
    val cmdBorderValue = BoolValue("CommandChatBorder", true)
    val fontType = FontValue("Font", Fonts.font40) { fontChatValue.get() }
    val chatRectValue = BoolValue("ChatRect", true)
    val chatCombineValue = BoolValue("ChatCombine", true)
    val chatAnimationValue = BoolValue("ChatAnimation", true)
    val chatAnimationSpeedValue = FloatValue("Chat-AnimationSpeed", 0.1F, 0.01F, 0.1F)

    private val toggleMessageValue = BoolValue("DisplayToggleMessage", false)
    private val toggleSoundValue = ListValue("ToggleSound", arrayOf("None", "Default", "Custom"), "Default")
    private val toggleVolumeValue = IntegerValue("ToggleVolume", 100, 0, 100) {
        toggleSoundValue.get().equals("custom", true)
    }
    val guiButtonStyle = ListValue("Button-Style", arrayOf("Vanilla", "Rounded", "LiquidBounce", "LiquidBounce+"), "Vanilla")

    val containerBackground = BoolValue("Container-Background", false)
    val containerButton = ListValue("Container-Button", arrayOf("TopLeft", "TopRight", "Off"), "TopLeft")
    val invEffectOffset = BoolValue("InvEffect-Offset", false)
    val domainValue = TextValue("Scoreboard-Domain", ".hud scoreboard-domain <your domain here>")


    private var hotBarX = 0F

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner) return
        MinusBounce.hud.render(false)
    }

    @EventTarget(ignoreCondition = true)
    fun onTick(event: TickEvent) {
        if (MinusBounce.moduleManager.shouldNotify != toggleMessageValue.get())
            MinusBounce.moduleManager.shouldNotify = toggleMessageValue.get()

        if (MinusBounce.moduleManager.toggleSoundMode != toggleSoundValue.values.indexOf(toggleSoundValue.get()))
            MinusBounce.moduleManager.toggleSoundMode = toggleSoundValue.values.indexOf(toggleSoundValue.get())

        if (MinusBounce.moduleManager.toggleVolume != toggleVolumeValue.get().toFloat())
            MinusBounce.moduleManager.toggleVolume = toggleVolumeValue.get().toFloat()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        MinusBounce.hud.update()
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        MinusBounce.hud.handleKey('a', event.key)
    }

    fun getAnimPos(pos: Float): Float {
        hotBarX = if (state && animHotbarValue.get()) AnimationUtils.animate(
            pos,
            hotBarX,
            0.02F * RenderUtils.deltaTime.toFloat()
        )
        else pos

        return hotBarX
    }

    fun getColor(alpha: Int? = null): Color {
        val red = redValue.get()
        val green = greenValue.get()
        val blue = blueValue.get()
        val alphaValue = alpha ?: 255

        return Color(red, green, blue, alphaValue)
    }

    fun setColor(type: String, alpha: Int): Serializable {
        if (themeMode.get().equals("custom", true)) return getColor()
        when (themeMode.get().lowercase()) {

            "cherry" -> if (type == "START") {
                return Color(215, 171, 168, alpha)
            } else if (type == "END") {
                return Color(206, 58, 98, alpha)
            }

            "water" -> if (type == "START") {
                return Color(108, 170, 207, alpha)
            } else if (type == "END") {
                return Color(35, 69, 148, alpha)
            }

            "magic" -> if (type == "START") {
                return Color(255, 180, 255, alpha)
            } else if (type == "END") {
                return Color(192, 67, 255, alpha)
            }

            "darknight" -> if (type == "START") {
                return Color(203, 200, 204, alpha)
            } else if (type == "END") {
                return Color(93, 95, 95, alpha)
            }

            "sun" -> if (type == "START") {
                return Color(252, 205, 44, alpha)
            } else if (type == "END") {
                return Color(255, 143, 0, alpha)
            }

            "flower" -> if (type == "START") {
                return Color(182, 140, 195, alpha)
            } else if (type == "END") {
                return Color(184, 85, 199, alpha)
            }

            "tree" -> if (type == "START") {
                return Color(76, 255, 102, alpha)
            } else if (type == "END") {
                return Color(18, 155, 38, alpha)
            }

            "loyoi" -> if (type == "START") {
                return Color(255, 131, 124, alpha)
            } else if (type == "END") {
                return Color(255, 131, 0, alpha)
            }

            "soniga" -> if (type == "START") {
                return Color(100, 255, 255, alpha)
            } else if (type == "END") {
                return Color(255, 100, 255, alpha)
            }
            "may" -> if (type == "START") {
                return Color(255, 255, 255, alpha)
            } else if (type == "END") {
                return Color(255, 80, 255, alpha)
            }
            "mint" -> if (type == "START") {
                return Color(85, 255, 255, alpha)
            } else if (type == "END") {
                return Color(85, 255, 140, alpha)
            }
            "cero" -> if (type == "START") {
                return Color(170, 255, 170, alpha)
            } else if (type == "END") {
                return Color(170, 0, 170, alpha)
            }
            "azure" -> if (type == "START") {
                return Color(0, 180, 255, alpha)
            } else if (type == "END") {
                return Color(0, 90, 255, alpha)
            }

        }

        return Color(-1)
    }

    fun getColor(index: Int): Color {
        if (themeMode.get().equals("custom", true)) return getColor()
        when (themeMode.get().lowercase()) {
            "cherry" -> return ColorUtils.mixColors(
                Color(206, 58, 98),
                Color(215, 171, 168),
                fadespeed.get() / 5.0 * if (updown.get()) 1 else -1,
                index
            )

            "water" -> return ColorUtils.mixColors(
                Color(35, 69, 148),
                Color(108, 170, 207),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "magic" -> return ColorUtils.mixColors(
                Color(255, 180, 255),
                Color(181, 139, 194),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "tree" -> return ColorUtils.mixColors(
                Color(18, 155, 38),
                Color(76, 255, 102),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "darknight" -> return ColorUtils.mixColors(
                Color(93, 95, 95),
                Color(203, 200, 204),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "sun" -> return ColorUtils.mixColors(
                Color(255, 143, 0),
                Color(252, 205, 44),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "flower" -> return ColorUtils.mixColors(
                Color(184, 85, 199),
                Color(182, 140, 195),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "loyoi" -> return ColorUtils.mixColors(
                Color(255, 131, 0),
                Color(255, 131, 124),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
            "soniga" -> return ColorUtils.mixColors(
                Color(255, 100, 255),
                Color(100, 255, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "may" -> return ColorUtils.mixColors(
                Color(255, 80, 255),
                Color(255, 255, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
            "mint" -> return ColorUtils.mixColors(
                Color(85, 255, 140),
                Color(85, 255, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
            "cero" -> return ColorUtils.mixColors(
                Color(170, 0, 170),
                Color(170, 255, 170),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
            "azure" -> return ColorUtils.mixColors(
                Color(0, 90, 255),
                Color(0, 180, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

        }
        return Color(-1)
    }
    fun getColorFromName(name: String,index: Int): Color {
        if (themeMode.get().equals("custom", true)) return getColor()
        when (name.lowercase()) {
            "cherry" -> return ColorUtils.mixColors(
                Color(206, 58, 98),
                Color(215, 171, 168),
                fadespeed.get() / 5.0 * if (updown.get()) 1 else -1,
                index
            )

            "water" -> return ColorUtils.mixColors(
                Color(35, 69, 148),
                Color(108, 170, 207),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "magic" -> return ColorUtils.mixColors(
                Color(255, 180, 255),
                Color(181, 139, 194),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "tree" -> return ColorUtils.mixColors(
                Color(18, 155, 38),
                Color(76, 255, 102),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "darknight" -> return ColorUtils.mixColors(
                Color(93, 95, 95),
                Color(203, 200, 204),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "sun" -> return ColorUtils.mixColors(
                Color(255, 143, 0),
                Color(252, 205, 44),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "flower" -> return ColorUtils.mixColors(
                Color(184, 85, 199),
                Color(182, 140, 195),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )

            "loyoi" -> return ColorUtils.mixColors(
                Color(255, 131, 0),
                Color(255, 131, 124),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
            "soniga" -> return ColorUtils.mixColors(
                Color(255, 100, 255),
                Color(100, 255, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
            "may" -> return ColorUtils.mixColors(
                Color(255, 80, 255),
                Color(255, 255, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
            "mint" -> return ColorUtils.mixColors(
                Color(85, 255, 140),
                Color(85, 255, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
            "cero" -> return ColorUtils.mixColors(
                Color(170, 0, 170),
                Color(170, 255, 170),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
            "azure" -> return ColorUtils.mixColors(
                Color(0, 90, 255),
                Color(0, 180, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            )
        }
        return Color(-1)
    }
    fun getColorWithAlpha(index: Int, alpha: Int): Color {
        if (themeMode.get().equals("custom", true)) return getColor()
        when (themeMode.get().lowercase()) {
            "cherry" -> return ColorUtils.mixColors(
                Color(206, 58, 98),
                Color(215, 171, 168),
                fadespeed.get() / 5.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)

            "water" -> return ColorUtils.mixColors(
                Color(35, 69, 148),
                Color(108, 170, 207),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)

            "magic" -> return ColorUtils.mixColors(
                Color(255, 180, 255),
                Color(181, 139, 194),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)

            "tree" -> return ColorUtils.mixColors(
                Color(18, 155, 38),
                Color(76, 255, 102),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)

            "darknight" -> return ColorUtils.mixColors(
                Color(93, 95, 95),
                Color(203, 200, 204),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)

            "sun" -> return ColorUtils.mixColors(
                Color(255, 143, 0),
                Color(252, 205, 44),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)

            "flower" -> return ColorUtils.mixColors(
                Color(184, 85, 199),
                Color(182, 140, 195),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)

            "loyoi" -> return ColorUtils.mixColors(
                Color(255, 131, 0),
                Color(255, 131, 124),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)
            "soniga" -> return ColorUtils.mixColors(
                Color(255, 100, 255),
                Color(100, 255, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)
            "may" -> return ColorUtils.mixColors(
                Color(255, 80, 255),
                Color(255, 255, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)
            "mint" -> return ColorUtils.mixColors(
                Color(85, 255, 180),
                Color(85, 255, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)
            "cero" -> return ColorUtils.mixColors(
                Color(170, 0, 170),
                Color(170, 255, 170),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)
            "azure" -> return ColorUtils.mixColors(
                Color(0, 90, 255),
                Color(0, 180, 255),
                fadespeed.get() / 7.0 * if (updown.get()) 1 else -1,
                index
            ).setAlpha(alpha)
        }
        return Color(-1)
    }

    init {
        state = true
    }
}
