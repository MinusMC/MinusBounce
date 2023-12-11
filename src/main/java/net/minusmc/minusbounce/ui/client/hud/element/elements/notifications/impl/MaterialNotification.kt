package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.Type
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.Stencil
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

class MaterialNotification: NotificationStyle("Material") {
    override fun drawStyle() {
        val x = notification.x
        val textLength = notification.textLength
        val blur = inst.blurValue.get()
        val originalX = inst.renderX.toFloat()
        val originalY = inst.renderY.toFloat()
        val strength = inst.blurStrength.get()
        val backgroundColor = Color(0, 0, 0, inst.bgAlphaValue.get())
        
        GlStateManager.resetColor()

        GL11.glPushMatrix()
        GL11.glTranslatef(-x, -y - notifHeight - (if (barMaterial) 2F else 0F), 0F)

        RenderUtils.originalRoundedRect(1F, -1F, 159F, notifHeight + (if (barMaterial) 2F else 0F) + 1F, 1F, when (type) {
                Type.SUCCESS -> Color(72, 210, 48, 70).rgb
                Type.ERROR -> Color(227, 28, 28, 70).rgb
                Type.WARNING -> Color(245, 212, 25, 70).rgb
                Type.INFO -> Color(255, 255, 255, 70).rgb
            })
        RenderUtils.originalRoundedRect(-1F, 1F, 161F, notifHeight + (if (barMaterial) 2F else 0F) - 1F, 1F, when (type) {
                Type.SUCCESS -> Color(72, 210, 48, 70).rgb
                Type.ERROR -> Color(227, 28, 28, 70).rgb
                Type.WARNING -> Color(245, 212, 25, 70).rgb
                Type.INFO -> Color(255, 255, 255, 70).rgb
            })
        RenderUtils.originalRoundedRect(-0.5F, -0.5F, 160.5F, notifHeight + (if (barMaterial) 2F else 0F) + 0.5F, 1F, when (type) {
                Type.SUCCESS -> Color(72, 210, 48, 80).rgb
                Type.ERROR -> Color(227, 28, 28, 80).rgb
                Type.WARNING -> Color(245, 212, 25, 80).rgb
                Type.INFO -> Color(255, 255, 255, 80).rgb
            })

        if (barMaterial) {
            Stencil.write(true)
            RenderUtils.originalRoundedRect(0F, 0F, 160F, notifHeight + 2F, 1F, when (type) {
                Type.SUCCESS -> Color(72, 210, 48, 255).rgb
                Type.ERROR -> Color(227, 28, 28, 255).rgb
                Type.WARNING -> Color(245, 212, 25, 255).rgb
                Type.INFO -> Color(255, 255, 255, 255).rgb
            })
            Stencil.erase(true)
            if (fadeState == FadeState.STAY) RenderUtils.newDrawRect(0F, notifHeight, 160F * if (stayTimer.hasTimePassed(displayTime)) 1F else ((System.currentTimeMillis() - stayTimer.time).toFloat() / displayTime.toFloat()), notifHeight + 2F, when (type) {
                Type.SUCCESS -> Color(72 + 90, 210 + 30, 48 + 90, 255).rgb
                Type.ERROR -> Color(227 + 20, 28 + 90, 28 + 90, 255).rgb
                Type.WARNING -> Color(245 - 70, 212 - 70, 25, 255).rgb
                Type.INFO -> Color(155, 155, 155, 255).rgb
            })
            Stencil.dispose()
        } else RenderUtils.originalRoundedRect(0F, 0F, 160F, notifHeight, 1F, when (type) {
            Type.SUCCESS -> Color(72, 210, 48, 255).rgb
            Type.ERROR -> Color(227, 28, 28, 255).rgb
            Type.WARNING -> Color(245, 212, 25, 255).rgb
            Type.INFO -> Color(255, 255, 255, 255).rgb
        })

        var yHeight = 7F
        for (s in messageList) {
            Fonts.font40.drawString(s, 30F, yHeight, if (type == Type.ERROR) -1 else 0)
            yHeight += Fonts.font40.FONT_HEIGHT.toFloat() + 2F
        }

        GL11.glPushMatrix()
        GlStateManager.disableAlpha()
        RenderUtils.drawImage3(when (type) {
            Type.SUCCESS -> newSuccess
            Type.ERROR -> newError
            Type.WARNING -> newWarning
            Type.INFO -> newInfo
        }, 9F, notifHeight / 2F - 6F, 12, 12,
        if (type == Type.ERROR) 1F else 0F,
        if (type == Type.ERROR) 1F else 0F,
        if (type == Type.ERROR) 1F else 0F, 1F)
        GlStateManager.enableAlpha()
        GL11.glPopMatrix()

        GL11.glPopMatrix()

        GlStateManager.resetColor()
    }

    override fun drawNotifications(notifications: MutableList<Notification>, yPos: Float) {
        var yPos = yPos
        var idx = 0
        for (notification in notifications) {
            if (indexz == 0 && side.vertical != Side.Vertical.DOWN)
                yPos -= i.notifHeight - (if (inst.barValue.get()) 2F else 0F)

            drawNotification(yPos, this)
            if (indexz < notifications.size - 1) indexz++

            if (side.vertical == Side.Vertical.DOWN)
                yPos += this.animationY
            else 
                yPos -= this.animationY
        }
    }

    override fun drawElement(): Border? {
        if (mc.currentScreen !is GuiHudDesigner || notifications.isNotEmpty())
            drawNotifications(notifications, 30F)
        else
            exampleNotification.drawNotification(30f - if (side.vertical != Side.Vertical.DOWN) (exampleNotification.notifHeight - 5F - (if (barValue.get()) 2F else 0F)) else 0F, this)

        if (mc.currentScreen is GuiHudDesigner) {
            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.x = 160F

            if (exampleNotification.stayTimer.hasTimePassed(exampleNotification.displayTime)) 
                exampleNotification.stayTimer.reset()

            return if (side.vertical == Side.Vertical.DOWN) Border(-160F, -50F, 0F, -30F) else Border(-160F, -20F, 0F, 0F)
        }

        return null
    }

    override val animationY: Float
        get() = (if (side.vertical == Side.Vertical.DOWN) i.notifHeight 
                else notifications[indexz].notifHeight) + 5F + (if (barValue.get()) 2F else 0F)

    override val border: Border?
        get() = if (side.vertical == Side.Vertical.DOWN) Border(-160F, -50F, 0F, -30F) else Border(-160F, -20F, 0F, 0F)
}

