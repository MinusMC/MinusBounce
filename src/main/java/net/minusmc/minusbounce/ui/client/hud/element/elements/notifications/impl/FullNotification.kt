package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.Type
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.Stencil
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color


class FullNotification: NotificationStyle("Full") {
    private val notifyDir = "minusbounce/notification/"
    private val imgSuccess = ResourceLocation("${notifyDir}checkmark.png")
    private val imgError = ResourceLocation("${notifyDir}error.png")
    private val imgWarning = ResourceLocation("${notifyDir}warning.png")
    private val imgInfo = ResourceLocation("${notifyDir}info.png")

	override fun drawStyle() {
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
        RenderUtils.drawImage2(when (type) {
            Type.SUCCESS -> imgSuccess
            Type.ERROR -> imgError
            Type.WARNING -> imgWarning
            Type.INFO -> imgInfo
        }, kek, -27F - y, 26, 26)
        GlStateManager.enableAlpha()
        GL11.glPopMatrix()

        GlStateManager.resetColor()
        if (fadeState == FadeState.STAY && !stayTimer.hasTimePassed(displayTime))
            RenderUtils.drawRect(kek, -y, kek + (dist * if (stayTimer.hasTimePassed(displayTime)) 0F else ((displayTime - (System.currentTimeMillis() - stayTimer.time)).toFloat() / displayTime.toFloat())), -1F - y, enumColor)
        else if (fadeState == FadeState.IN)
            RenderUtils.drawRect(kek, -y, kek + dist, -1F - y, enumColor)

        GlStateManager.resetColor()
        Fonts.font40.drawString(message, -x + 2, -18F - y, -1)
	}
}