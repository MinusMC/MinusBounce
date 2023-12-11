package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.ui.client.hud.element.elements.Notifications
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.Type
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

class CompactNotification(inst: Notifications): NotificationStyle("Compact", inst) {
    private val bgAlphaValue = IntegerValue("Background-Alpha", 120, 0, 255)
    private val blurValue = BoolValue("Blur", false)
    private val blurStrength = FloatValue("Strength", 0F, 0F, 30F)

	override fun drawStyle(notification: Notification, y: Float) {
        val x = notification.x
        val textLength = notification.textLength
        val blur = blurValue.get()
        val originalX = inst.renderX.toFloat()
        val originalY = inst.renderY.toFloat()
        val strength = blurStrength.get()
        val backgroundColor = Color(0, 0, 0, bgAlphaValue.get())
        
		GlStateManager.resetColor()

        if (blur) {
            GL11.glTranslatef(-originalX, -originalY, 0F)
            GL11.glPushMatrix()
            BlurUtils.blurAreaRounded(originalX + -x - 5F, originalY + -18F - y, originalX + -x + 8F + textLength, originalY + -y, 3F, strength)
            GL11.glPopMatrix()
            GL11.glTranslatef(originalX, originalY, 0F)
        }

        RenderUtils.customRounded(-x + 8F + textLength, -y, -x - 2F, -18F - y, 0F, 3F, 3F, 0F, backgroundColor.rgb)
        RenderUtils.customRounded(-x - 2F, -y, -x - 5F, -18F - y, 3F, 0F, 0F, 3F, when (notification.type) {
            Type.SUCCESS -> Color(80, 255, 80).rgb
            Type.ERROR -> Color(255, 80, 80).rgb
            Type.INFO -> Color(255, 255, 255).rgb
            Type.WARNING -> Color(255, 255, 0).rgb
        })

        GlStateManager.resetColor()
        Fonts.font40.drawString(notification.message, -x + 3, -13F - y, -1)
	}

    override val animationY = 20f
    override val border = Border(-102F, -48F, 0F, -30F)
}