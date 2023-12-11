/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.hud.element.elements

import net.minecraft.client.renderer.GlStateManager
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

@ElementInfo(name = "Notifications", single = true)
class Notifications(x: Double = 0.0, y: Double = 30.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    val styleValue = ListValue("Style", arrayOf("Full", "Full2", "Compact", "Material", "Test"), "Material")

    val hAnimModeValue = ListValue("H-Animation", arrayOf("LiquidBounce", "Smooth"), "LiquidBounce")
    val vAnimModeValue = ListValue("V-Animation", arrayOf("None", "Smooth"), "Smooth")
    val animationSpeed = FloatValue("Speed", 0.5F, 0.01F, 1F) {
        hAnimModeValue.get().equals("smooth", true) || vAnimModeValue.get().equals("smooth", true)
    }

    /**
     * Draw element
     */
    override fun drawElement(): Border? = style.drawElement()

    // private fun getNotifBorder() = when (styleValue.get().lowercase()) {
    //     "test" -> Border(-130F, -58F, 0F, -30F)
    // }
}

class Notification(val message: String, val type: Type, val displayTime: Long) {

    constructor(message: String, type: Type) : this(message, type, 2000L)
    constructor(message: String) : this(message, Type.INFO, 500L)
    constructor(message: String, displayTime: Long) : this(message, Type.INFO, displayTime)

    var x = 0F
    var textLength = 0
    var fadeState = FadeState.IN
    var messageList = Fonts.font40.listFormattedStringToWidth(message, 105)
    var stayTimer = MSTimer()
    
    private var stay = 0F
    private var fadeStep = 0F
    private var firstY = 0f
    var notifHeight = 0f

    init {
        firstY = 19190F
        stayTimer.reset()
        textLength = Fonts.font40.getStringWidth(message)
        messageList = Fonts.font40.listFormattedStringToWidth(notification.message, 105)
        notifHeight = messageList.size.toFloat() * (Fonts.font40.FONT_HEIGHT.toFloat() + 2F) + 8F
    }

    enum class Type {
        SUCCESS, INFO, WARNING, ERROR
    }

    enum class FadeState {
        IN, STAY, OUT, END
    }

    fun drawNotification(animationY: Float, parent: Notifications) {
        val delta = RenderUtils.deltaTime

        val style = parent.styleValue.get()

        val hAnimMode = parent.hAnimModeValue.get()
        val vAnimMode = parent.vAnimModeValue.get()
        val animSpeed = parent.animationSpeed.get()

        val width = if (style.equals("material", true)) 160F else textLength.toFloat() + 8.0f

        firstY = if (vAnimMode.equals("smooth", true)) {
            if (firstY == 19190.0F)
                animationY
            else
                AnimationUtils.animate(animationY, firstY, 0.02F * delta)
        } else {
            animationY
        }

        val y = firstY

        // draw style
        style.drawStyle(this, y)

        when (fadeState) {
            FadeState.IN -> {
                if (x < width) {
                    x = if (hAnimMode.equals("smooth", true))
                        net.minusmc.minusbounce.utils.render.AnimationUtils.animate(width, x, animSpeed * 0.025F * delta)
                    else
                        AnimationUtils.easeOut(fadeStep, width) * width
                    fadeStep += delta / 4F
                }
                if (x >= width) {
                    fadeState = FadeState.STAY
                    x = width
                    fadeStep = width
                }

                stay = 60F
                stayTimer.reset()
            }

            FadeState.STAY -> {
                if (stay > 0) {
                    stay = 0F
                    stayTimer.reset()
                }
                if (stayTimer.hasTimePassed(displayTime))
                    fadeState = FadeState.OUT
            }

            FadeState.OUT -> if (x > 0) {
                x = if (hAnimMode.equals("smooth", true))
                    net.minusmc.minusbounce.utils.render.AnimationUtils.animate(-width / 2F, x, animSpeed * 0.025F * delta)
                else
                    AnimationUtils.easeOut(fadeStep, width) * width

                fadeStep -= delta / 4F
            } else
                fadeState = FadeState.END

            FadeState.END -> hud.removeNotification(this)
        }
    }
}
