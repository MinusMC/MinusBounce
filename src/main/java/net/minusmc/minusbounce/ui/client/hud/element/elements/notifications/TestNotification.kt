package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification.Type
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.Stencil
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

class TestNotification: NotificationStyle("Test") {
	override fun drawStyle() {
		val kek = -x - 1 - 20F

        GlStateManager.resetColor()
        if (blur) {
            GL11.glTranslatef(-originalX, -originalY, 0F)
            GL11.glPushMatrix()
            BlurUtils.blurAreaRounded(originalX + kek, originalY + -28F - y, originalX + -x + 8 + textLength, originalY + -y, 3F, strength)
            GL11.glPopMatrix()
            GL11.glTranslatef(originalX, originalY, 0F)
        }

        Stencil.write(true)

        when (type) {
            Type.ERROR -> {
                RenderUtils.drawRoundedRect(-x + 9 + textLength, -y + 1, kek - 1, -28F - y - 1, 0F, Color(115,69,75).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, -y, kek, -28F - y, 0F, Color(89,61,65).rgb)
                Fonts.minecraftFont.drawStringWithShadow("Error:", -x - 4, -25F - y, Color(249,130,108).rgb)
            }
            Type.INFO -> {
                RenderUtils.drawRoundedRect(-x + 9 + textLength, -y + 1, kek - 1, -28F - y - 1, 0F, Color(70,94,115).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, -y, kek, -28F - y, 0F, Color(61,72,87).rgb)
                Fonts.minecraftFont.drawStringWithShadow("Information:", -x - 4, -25F - y, Color(119,145,147).rgb)
            }
            Type.SUCCESS -> {
                RenderUtils.drawRoundedRect(-x + 9 + textLength, -y + 1, kek - 1, -28F - y - 1, 0F, Color(67,104,67).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, -y, kek, -28F - y, 0F, Color(55,78,55).rgb)
                Fonts.minecraftFont.drawStringWithShadow("Success:", -x - 4, -25F - y, Color(10,142,2).rgb)
            }
            Type.WARNING -> {
                RenderUtils.drawRoundedRect(-x + 9 + textLength, -y + 1, kek - 1, -28F - y - 1, 0F, Color(103,103,63).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, -y, kek, -28F - y, 0F, Color(80,80,57).rgb)
                Fonts.minecraftFont.drawStringWithShadow("Warning:", -x - 4, -25F - y, Color(175,163,0).rgb)
            }
        }

        Stencil.erase(true)
        GlStateManager.resetColor()
        Stencil.dispose()

        GL11.glPushMatrix()
        GlStateManager.disableAlpha()
        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)
        RenderUtils.drawImage2(when (type) {
            Type.SUCCESS -> imgSuccess
            Type.ERROR -> imgError
            Type.WARNING -> imgWarning
            Type.INFO -> imgInfo
        }, kek + 5, -25F - y, 7, 7)
        GlStateManager.enableAlpha()
        GL11.glPopMatrix()

        Fonts.minecraftFont.drawStringWithShadow(message, -x - 4, -13F - y, -1)
	}
}