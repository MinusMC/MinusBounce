package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.ui.client.hud.element.elements.Notifications
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.Type
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.FadeState
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

class Full2Notification(inst: Notifications): NotificationStyle("Full2", inst) {
    private val bgAlphaValue = IntegerValue("Background-Alpha", 120, 0, 255)
    private val blurValue = BoolValue("Blur", false)
    private val blurStrength = FloatValue("Strength", 0F, 0F, 30F)

    private val notifyDir = "minusbounce/notification/"
    private val newSuccess = ResourceLocation("${notifyDir}new/checkmark.png")
    private val newError = ResourceLocation("${notifyDir}new/error.png")
    private val newWarning = ResourceLocation("${notifyDir}new/warning.png")
    private val newInfo = ResourceLocation("${notifyDir}new/info.png")

	override fun drawStyle(notification: Notification, y: Float) {
        val x = notification.x
        val originalX = inst.renderX.toFloat()
        val originalY = inst.renderY.toFloat()
        val textLength = notification.textLength
        val blur = blurValue.get()
        val strength = blurStrength.get()
        val backgroundColor = Color(0, 0, 0, bgAlphaValue.get())

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
            BlurUtils.blurAreaRounded(originalX + kek, originalY + -28F - y, originalX + -x + 8 + textLength, originalY + -y, 1.8f, strength)
            GL11.glPopMatrix()
            GL11.glTranslatef(originalX, originalY, 0F)
        }

        RenderUtils.drawRoundedRect(-x + 8 + textLength, -y, kek, -28F - y, 1.8f, backgroundColor.rgb)

        GL11.glPushMatrix()
        GlStateManager.disableAlpha()
        RenderUtils.drawImage2(when (notification.type) {
            Type.SUCCESS -> newSuccess
            Type.ERROR -> newError
            Type.WARNING -> newWarning
            Type.INFO -> newInfo
        }, kek, -27F - y, 26, 26)
        GlStateManager.enableAlpha()
        GL11.glPopMatrix()

        GlStateManager.resetColor()
        if (notification.fadeState == FadeState.STAY && !notification.stayTimer.hasTimePassed(notification.displayTime))
            RenderUtils.drawRoundedRect(kek, -y, kek + (dist * if (notification.stayTimer.hasTimePassed(notification.displayTime)) 0F else ((notification.displayTime - (System.currentTimeMillis() - notification.stayTimer.time)).toFloat() / notification.displayTime.toFloat())), -1F - y, 1.8f, enumColor)
        else if (notification.fadeState == FadeState.IN)
            RenderUtils.drawRoundedRect(kek, -y, kek + dist, -1F - y, 1.8f, enumColor)

        GlStateManager.resetColor()
        Fonts.fontSFUI40.drawStringWithShadow(notification.message, -x + 2, -18F - y, enumColor)
	}

    override val animationY = 30f
    override val border: Border
        get() = Border(-130F, -58F, 0F, -30F)
}