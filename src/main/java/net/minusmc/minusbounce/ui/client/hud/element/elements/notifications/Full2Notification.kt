package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

class Full2Notification: NotificationStyle("Full2") {
	override fun drawStyle() {
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
        RenderUtils.drawImage2(when (type) {
            Type.SUCCESS -> newSuccess
            Type.ERROR -> newError
            Type.WARNING -> newWarning
            Type.INFO -> newInfo
        }, kek, -27F - y, 26, 26)
        GlStateManager.enableAlpha()
        GL11.glPopMatrix()

        GlStateManager.resetColor()
        if (fadeState == FadeState.STAY && !stayTimer.hasTimePassed(displayTime))
            RenderUtils.drawRoundedRect(kek, -y, kek + (dist * if (stayTimer.hasTimePassed(displayTime)) 0F else ((displayTime - (System.currentTimeMillis() - stayTimer.time)).toFloat() / displayTime.toFloat())), -1F - y, 1.8f, enumColor)
        else if (fadeState == FadeState.IN)
            RenderUtils.drawRoundedRect(kek, -y, kek + dist, -1F - y, 1.8f, enumColor)

        GlStateManager.resetColor()
        Fonts.fontSFUI40.drawStringWithShadow(message, -x + 2, -18F - y, enumColor)
	}
}