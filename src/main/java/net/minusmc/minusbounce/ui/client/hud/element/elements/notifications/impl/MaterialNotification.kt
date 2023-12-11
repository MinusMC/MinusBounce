package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.ui.client.hud.element.elements.Notifications
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.Type
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.FadeState
import net.minusmc.minusbounce.ui.client.hud.element.Side
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.Stencil
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

class MaterialNotification(inst: Notifications): NotificationStyle("Material", inst) {
    val barValue = BoolValue("Bar", true)

    private val notifyDir = "minusbounce/notification/"
    private val newSuccess = ResourceLocation("${notifyDir}new/checkmark.png")
    private val newError = ResourceLocation("${notifyDir}new/error.png")
    private val newWarning = ResourceLocation("${notifyDir}new/warning.png")
    private val newInfo = ResourceLocation("${notifyDir}new/info.png")

    override fun drawStyle(notification: Notification, y: Float) {
        val x = notification.x
        val textLength = notification.textLength
        val originalX = inst.renderX.toFloat()
        val originalY = inst.renderY.toFloat()
        val barMaterial = barValue.get()
        GlStateManager.resetColor()

        GL11.glPushMatrix()
        GL11.glTranslatef(-x, -y - notification.notifHeight - (if (barMaterial) 2F else 0F), 0F)

        RenderUtils.originalRoundedRect(1F, -1F, 159F, notification.notifHeight + (if (barMaterial) 2F else 0F) + 1F, 1F, when (notification.type) {
                Type.SUCCESS -> Color(72, 210, 48, 70).rgb
                Type.ERROR -> Color(227, 28, 28, 70).rgb
                Type.WARNING -> Color(245, 212, 25, 70).rgb
                Type.INFO -> Color(255, 255, 255, 70).rgb
            })
        RenderUtils.originalRoundedRect(-1F, 1F, 161F, notification.notifHeight + (if (barMaterial) 2F else 0F) - 1F, 1F, when (notification.type) {
                Type.SUCCESS -> Color(72, 210, 48, 70).rgb
                Type.ERROR -> Color(227, 28, 28, 70).rgb
                Type.WARNING -> Color(245, 212, 25, 70).rgb
                Type.INFO -> Color(255, 255, 255, 70).rgb
            })
        RenderUtils.originalRoundedRect(-0.5F, -0.5F, 160.5F, notification.notifHeight + (if (barMaterial) 2F else 0F) + 0.5F, 1F, when (notification.type) {
                Type.SUCCESS -> Color(72, 210, 48, 80).rgb
                Type.ERROR -> Color(227, 28, 28, 80).rgb
                Type.WARNING -> Color(245, 212, 25, 80).rgb
                Type.INFO -> Color(255, 255, 255, 80).rgb
            })

        if (barMaterial) {
            Stencil.write(true)
            RenderUtils.originalRoundedRect(0F, 0F, 160F, notification.notifHeight + 2F, 1F, when (notification.type) {
                Type.SUCCESS -> Color(72, 210, 48, 255).rgb
                Type.ERROR -> Color(227, 28, 28, 255).rgb
                Type.WARNING -> Color(245, 212, 25, 255).rgb
                Type.INFO -> Color(255, 255, 255, 255).rgb
            })
            Stencil.erase(true)
            if (notification.fadeState == FadeState.STAY) RenderUtils.newDrawRect(0F, notification.notifHeight, 160F * if (notification.stayTimer.hasTimePassed(notification.displayTime)) 1F else ((System.currentTimeMillis() - notification.stayTimer.time).toFloat() / notification.displayTime.toFloat()), notification.notifHeight + 2F, when (notification.type) {
                Type.SUCCESS -> Color(72 + 90, 210 + 30, 48 + 90, 255).rgb
                Type.ERROR -> Color(227 + 20, 28 + 90, 28 + 90, 255).rgb
                Type.WARNING -> Color(245 - 70, 212 - 70, 25, 255).rgb
                Type.INFO -> Color(155, 155, 155, 255).rgb
            })
            Stencil.dispose()
        } else RenderUtils.originalRoundedRect(0F, 0F, 160F, notification.notifHeight, 1F, when (notification.type) {
            Type.SUCCESS -> Color(72, 210, 48, 255).rgb
            Type.ERROR -> Color(227, 28, 28, 255).rgb
            Type.WARNING -> Color(245, 212, 25, 255).rgb
            Type.INFO -> Color(255, 255, 255, 255).rgb
        })

        var yHeight = 7F
        for (s in messageList) {
            Fonts.font40.drawString(s, 30F, yHeight, if (notification.type == Type.ERROR) -1 else 0)
            yHeight += Fonts.font40.FONT_HEIGHT.toFloat() + 2F
        }

        GL11.glPushMatrix()
        GlStateManager.disableAlpha()
        RenderUtils.drawImage3(when (notification.type) {
            Type.SUCCESS -> newSuccess
            Type.ERROR -> newError
            Type.WARNING -> newWarning
            Type.INFO -> newInfo
        }, 9F, notification.notifHeight / 2F - 6F, 12, 12,
        if (notification.type == Type.ERROR) 1F else 0F,
        if (notification.type == Type.ERROR) 1F else 0F,
        if (notification.type == Type.ERROR) 1F else 0F, 1F)
        GlStateManager.enableAlpha()
        GL11.glPopMatrix()

        GL11.glPopMatrix()

        GlStateManager.resetColor()
    }

    override fun drawNotifications(notifications: MutableList<Notification>, yPos: Float) {
        var yPos = yPos
        var idx = 0
        for (notification in notifications) {
            if (idx == 0 && inst.side.vertical != Side.Vertical.DOWN)
                yPos -= notification.notifHeight - (if (barValue.get()) 2F else 0F)

            notification.drawNotification(yPos, inst)
            if (idx < notifications.size - 1) idx++

            if (inst.side.vertical == Side.Vertical.DOWN)
                yPos += (if (inst.side.vertical == Side.Vertical.DOWN) notification.notifHeight 
                else notifications[idx].notifHeight) + 5F + (if (barValue.get()) 2F else 0F)
            else 
                yPos -= (if (inst.side.vertical == Side.Vertical.DOWN) notification.notifHeight 
                else notifications[idx].notifHeight) + 5F + (if (barValue.get()) 2F else 0F)
        }
    }

    override fun drawElement(notifications: MutableList<Notification>): Border? {
        if (mc.currentScreen !is GuiHudDesigner || notifications.isNotEmpty())
            drawNotifications(notifications, 30F)
        else
            exampleNotification.drawNotification(30f - if (inst.side.vertical != Side.Vertical.DOWN) (notification.notifHeight - 5F - (if (barValue.get()) 2F else 0F)) else 0F, inst)

        if (mc.currentScreen is GuiHudDesigner) {
            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.x = 160F

            if (exampleNotification.stayTimer.hasTimePassed(exampleNotification.displayTime)) 
                exampleNotification.stayTimer.reset()

            return if (inst.side.vertical == Side.Vertical.DOWN) Border(-160F, -50F, 0F, -30F) else Border(-160F, -20F, 0F, 0F)
        }

        return null
    }

    override val border: Border?
        get() = if (inst.side.vertical == Side.Vertical.DOWN) Border(-160F, -50F, 0F, -30F) else Border(-160F, -20F, 0F, 0F)
}

