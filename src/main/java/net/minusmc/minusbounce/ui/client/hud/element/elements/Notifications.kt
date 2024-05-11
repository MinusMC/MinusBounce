/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.hud.element.elements

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.MinusBounce.hud
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.Element
import net.minusmc.minusbounce.ui.client.hud.element.ElementInfo
import net.minusmc.minusbounce.ui.client.hud.element.Side
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.AnimationUtils
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.Stencil
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.pow

@ElementInfo(name = "Notifications", single = true)
class Notifications(x: Double = 0.0, y: Double = 30.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {
    val bgAlphaValue = IntegerValue("Background-Alpha", 120, 0, 255)

    val blurValue = BoolValue("Blur", false)
    val blurStrength = FloatValue("Strength", 0F, 0F, 30F) {blurValue.get()}

    val hAnimModeValue = ListValue("H-Animation", arrayOf("LiquidBounce", "Smooth"), "LiquidBounce")
    val vAnimModeValue = ListValue("V-Animation", arrayOf("None", "Smooth"), "Smooth")
    val animationSpeed = FloatValue("Speed", 0.5F, 0.01F, 1F) {
        hAnimModeValue.get().equals("smooth", true) || vAnimModeValue.get().equals("smooth", true)
    }

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Info", "This is a test", Notification.Type.INFO)

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        hud.notifications.map {it}.forEachIndexed {index, notify ->
            GL11.glPushMatrix()

            if (notify.drawNotification(25f + 40f * index)) {
                hud.notifications.remove(notify)
            }

            GL11.glPopMatrix()
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!hud.notifications.contains(exampleNotification))
                hud.addNotification(exampleNotification)

            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.alpha = 255
            exampleNotification.displayTime = System.currentTimeMillis()

            return Border(0f, 0f, 50f, 25f)
        } else if (hud.notifications.contains(exampleNotification))
            hud.notifications.remove(exampleNotification)

        return null
    }
}
class Notification(val title: String, val message: String, val type: Type, var displayTime: Long) {
    constructor(title: String, message: String) : this(title, message, Type.INFO, 500L)
    constructor(title: String, message: String, type: Type) : this(title, message, type, 2000L)
    constructor(title: String, message: String, displayTime: Long) : this(title, message, Type.INFO, displayTime)

    private val success_icon = ResourceLocation("minusbounce/notification/success.png")
    private val error_icon = ResourceLocation("minusbounce/notification/error.png")
    private val warning_icon = ResourceLocation("minusbounce/notification/warning.png")
    private val info_icon = ResourceLocation("minusbounce/notification/info.png")

    var fadeState = FadeState.IN
    var stayTimer = MSTimer()
    var alpha = 0

    enum class Type {
        SUCCESS, INFO, WARNING, ERROR
    }

    enum class FadeState {
        IN, STAY, OUT, END
    }

    fun drawNotification(animationY: Float): Boolean {

        when (fadeState) {
            FadeState.IN -> {
                if (alpha >= 213) {
                    fadeState = FadeState.STAY
                    alpha = 255
                    stayTimer.reset()
                } else alpha += 32
            }
            FadeState.STAY -> if (stayTimer.hasTimePassed(displayTime)) {
                fadeState = FadeState.OUT
                stayTimer.reset()
            }
            FadeState.OUT -> {
                alpha = 0
                fadeState = FadeState.END
            }
            FadeState.END -> return true
        }

        GlStateManager.resetColor()
        RenderUtils.drawRoundedRect(-5f, -animationY, -140f, -animationY - 35f, 2f, Color(30, 30, 30, alpha / 2).rgb)
        
        GL11.glPushMatrix()
        GlStateManager.disableAlpha()
        RenderUtils.drawImage2(
            when (type) {
                Type.SUCCESS -> success_icon
                Type.ERROR -> error_icon
                Type.WARNING -> warning_icon
                Type.INFO -> info_icon
            }, -138f, -animationY - 35f, 32, 32
        )
        GlStateManager.enableAlpha()
        GL11.glPopMatrix()

        GlStateManager.resetColor()
        Fonts.fontLexendBold40.drawString(title, -100f, -animationY - 30f, Color(255, 255, 255, alpha).rgb)
        Fonts.fontLexend35.drawString(message, -100f, -animationY - 15f, Color(255, 255, 255, alpha).rgb)
        
        return false
    }
}