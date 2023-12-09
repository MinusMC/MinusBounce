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

@ElementInfo(name = "Notifications", single = true)
class Notifications(x: Double = 0.0, y: Double = 30.0, scale: Float = 1F,
                    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    val styleValue = ListValue("Style", arrayOf("Full", "Full2", "Compact", "Material", "Test"), "Material")
    val barValue = BoolValue("Bar", true) { styleValue.get().equals("material", true) }
    val bgAlphaValue = IntegerValue("Background-Alpha", 120, 0, 255) { !styleValue.get().equals("material", true) }

    val blurValue = BoolValue("Blur", false) { !styleValue.get().equals("material", true) }
    val blurStrength = FloatValue("Strength", 0F, 0F, 30F) {
        !styleValue.get().equals("material", true) && blurValue.get()
    }

    val hAnimModeValue = ListValue("H-Animation", arrayOf("LiquidBounce", "Smooth"), "LiquidBounce")
    val vAnimModeValue = ListValue("V-Animation", arrayOf("None", "Smooth"), "Smooth")
    val animationSpeed = FloatValue("Speed", 0.5F, 0.01F, 1F) {
        hAnimModeValue.get().equals("smooth", true) || vAnimModeValue.get().equals("smooth", true)
    }

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Tested", Notification.Type.INFO)

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        var animationY = 30F
        val notifications = mutableListOf<Notification>()

        for (i in hud.notifications)
            notifications.add(i)
        
        if (mc.currentScreen !is GuiHudDesigner || notifications.isNotEmpty()) {
            var indexz = 0
            for (i in notifications) {
                if (indexz == 0 && styleValue.get().equals("material", true) && side.vertical != Side.Vertical.DOWN) animationY -= i.notifHeight - (if (barValue.get()) 2F else 0F)
                i.drawNotification(animationY, this)
                if (indexz < notifications.size - 1) indexz++
                animationY += (when (styleValue.get().lowercase()) {
                                    "compact" -> 20F
                                    "full" -> 30F
                                    "full2" -> 30F
                                    "test" -> 30F
                                    else -> (if (side.vertical == Side.Vertical.DOWN) i.notifHeight else notifications[indexz].notifHeight) + 5F + (if (barValue.get()) 2F else 0F)
                                }) * (if (side.vertical == Side.Vertical.DOWN) 1F else -1F)
            }
        } else {
            exampleNotification.drawNotification(animationY - if (styleValue.get().equals("material", true) && side.vertical != Side.Vertical.DOWN) (exampleNotification.notifHeight - 5F - (if (barValue.get()) 2F else 0F)) else 0F, this)
        }

        if (mc.currentScreen is GuiHudDesigner) {
            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.x = if (styleValue.get().equals("material", true)) 160F else exampleNotification.textLength + 8F

            if (exampleNotification.stayTimer.hasTimePassed(exampleNotification.displayTime)) 
                exampleNotification.stayTimer.reset()

            return getNotifBorder()
        }

        return null
    }

    private fun getNotifBorder() = when (styleValue.get().lowercase()) {
        "full" -> Border(-130F, -58F, 0F, -30F)
        "full2" -> Border(-130F, -58F, 0F, -30F)
        "test" -> Border(-130F, -58F, 0F, -30F)
        "compact" -> Border(-102F, -48F, 0F, -30F)
        else -> if (side.vertical == Side.Vertical.DOWN) Border(-160F, -50F, 0F, -30F) else Border(-160F, -20F, 0F, 0F)
    }
}

class Notification(message : String, type : Type, displayLength: Long) {
    private val notifyDir = "minusbounce/notification/"

    private val imgSuccess = ResourceLocation("${notifyDir}checkmark.png")
    private val imgError = ResourceLocation("${notifyDir}error.png")
    private val imgWarning = ResourceLocation("${notifyDir}warning.png")
    private val imgInfo = ResourceLocation("${notifyDir}info.png")

    private val newSuccess = ResourceLocation("${notifyDir}new/checkmark.png")
    private val newError = ResourceLocation("${notifyDir}new/error.png")
    private val newWarning = ResourceLocation("${notifyDir}new/warning.png")
    private val newInfo = ResourceLocation("${notifyDir}new/info.png")

    var x = 0F
    var textLength = 0
    var fadeState = FadeState.IN
    var displayTime = 0L
    var stayTimer = MSTimer()
    var notifHeight = 0F
    private var message = ""
    private var messageList : List<String>
    private var stay = 0F
    private var fadeStep = 0F
    private var firstY = 0f
    private var type: Type

    init {
        this.message = message
        this.messageList = Fonts.font40.listFormattedStringToWidth(message, 105)
        this.notifHeight = messageList.size.toFloat() * (Fonts.font40.FONT_HEIGHT.toFloat() + 2F) + 8F
        this.type = type
        this.displayTime = displayLength
        this.firstY = 19190F
        this.stayTimer.reset()
        this.textLength = Fonts.font40.getStringWidth(message)
    }

    constructor(message: String, type: Type) : this(message, type, 2000L)

    constructor(message: String) : this(message, Type.INFO, 500L)

    constructor(message: String, displayLength: Long) : this(message, Type.INFO, displayLength)

    enum class Type {
        SUCCESS, INFO, WARNING, ERROR
    }

    enum class FadeState {
        IN, STAY, OUT, END
    }

    fun drawNotification(animationY: Float, parent: Notifications) {
        val delta = RenderUtils.deltaTime

        val style = parent.styleValue.get()
        val barMaterial = parent.barValue.get()

        val blur = parent.blurValue.get()
        val strength = parent.blurStrength.get()

        val hAnimMode = parent.hAnimModeValue.get()
        val vAnimMode = parent.vAnimModeValue.get()
        val animSpeed = parent.animationSpeed.get()

        val originalX = parent.renderX.toFloat()
        val originalY = parent.renderY.toFloat()
        val width = if (style.equals("material", true)) 160F else textLength.toFloat() + 8.0f

        val backgroundColor = Color(0, 0, 0, parent.bgAlphaValue.get())
        val enumColor = when (type) {
            Type.SUCCESS -> Color(80, 255, 80).rgb
            Type.ERROR -> Color(255, 80, 80).rgb
            Type.INFO -> Color(255, 255, 255).rgb
            Type.WARNING -> Color(255, 255, 0).rgb
        }

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

        when (fadeState) {
            FadeState.IN -> {
                if (x < width) {
                    x = if (hAnimMode.equals("smooth", true))
                        net.minusmc.minusbounce.utils.AnimationUtils.animate(width, x, animSpeed * 0.025F * delta)
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
                    net.minusmc.minusbounce.utils.AnimationUtils.animate(-width / 2F, x, animSpeed * 0.025F * delta)
                else
                    AnimationUtils.easeOut(fadeStep, width) * width

                fadeStep -= delta / 4F
            } else
                fadeState = FadeState.END

            FadeState.END -> hud.removeNotification(this)
        }
    }
}
