package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.ui.client.hud.element.elements.Notifications
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.Type
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.FadeState
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.Stencil
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color


class FullNotification: NotificationStyle("Full", inst) {
    private val notifyDir = "minusbounce/notification/"
    private val imgSuccess = ResourceLocation("${notifyDir}checkmark.png")
    private val imgError = ResourceLocation("${notifyDir}error.png")
    private val imgWarning = ResourceLocation("${notifyDir}warning.png")
    private val imgInfo = ResourceLocation("${notifyDir}info.png")

	override fun drawStyle(notification: Notification, y: Float) {
        val x = notification.x
        val originalX = inst.renderX.toFloat()
        val originalY = inst.renderY.toFloat()
        val textLength = notification.textLength
        val blur = inst.blurValue.get()
        val strength = inst.blurStrength.get()
        val backgroundColor = Color(0, 0, 0, inst.bgAlphaValue.get())

        val enumColor = when (notification.type) {
            Type.SUCCESS -> Color(80, 255, 80).rgb
            Type.ERROR -> Color(255, 80, 80).rgb
            Type.INFO -> Color(255, 255, 255).rgb
            Type.WARNING -> Color(255, 255, 0).rgb
        }

		val dist = (x + 1 + 26F) - (x - 8 - textLength)
        val kek = -x - 1 - 26F

        GlStateManager.resetColor()

        if (blur) {
            GL11.glTranslatef(-originalX, -originalY, 0F)
            GL11.glPushMatrix()
            BlurUtils.blurArea(originalX + kek, originalY + -28F - y, originalX + -x + 8 + textLength, originalY + -y, strength)
            GL11.glPopMatrix()
            GL11.glTranslatef(originalX, originalY, 0F)
        }

        RenderUtils.drawRect(-x + 8 + textLength, -y, kek, -28F - y, backgroundColor.rgb)

        GL11.glPushMatrix()
        GlStateManager.disableAlpha()
        RenderUtils.drawImage2(when (notification.type) {
            Type.SUCCESS -> imgSuccess
            Type.ERROR -> imgError
            Type.WARNING -> imgWarning
            Type.INFO -> imgInfo
        }, kek, -27F - y, 26, 26)
        GlStateManager.enableAlpha()
        GL11.glPopMatrix()

        GlStateManager.resetColor()
        if (notification.fadeState == FadeState.STAY && !notification.stayTimer.hasTimePassed(notification.displayTime))
            RenderUtils.drawRect(kek, -y, kek + (dist * if (notification.stayTimer.hasTimePassed(notification.displayTime)) 0F else ((notification.displayTime - (System.currentTimeMillis() - notification.stayTimer.time)).toFloat() / notification.displayTime.toFloat())), -1F - y, enumColor)
        else if (notification.fadeState == FadeState.IN)
            RenderUtils.drawRect(kek, -y, kek + dist, -1F - y, enumColor)

        GlStateManager.resetColor()
        Fonts.font40.drawString(notification.message, -x + 2, -18F - y, -1)
	}
}