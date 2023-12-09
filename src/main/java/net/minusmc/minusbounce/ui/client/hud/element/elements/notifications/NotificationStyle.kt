package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.client.hud.element.Border

abstract class NotificationStyle(val styleName: String, val inst: Notifications): MinecraftInstance() {
	protected val exampleNotification = Notification("Tested", Notification.Type.INFO)
	open val animationY = 30f

	open fun drawStyle() {}

	open fun drawNotifications(notifications: MutableList<Notification>, yPos: Float) {
		var yPos = yPos
		var idx = 0
		for (notification in notifications) {
			notification.drawNotification(yPos, this)
            if (indexz < notifications.size - 1) indexz++
            if (side.vertical == Side.Vertical.DOWN)
                yPos += this.animationY
            else 
                yPos -= this.animationY
		}
	}

	open fun drawElement(): Border? {
        if (mc.currentScreen !is GuiHudDesigner || notifications.isNotEmpty()) {
            drawNotifications(notifications, 30F)
        else
            exampleNotification.drawNotification(30f, this)

        if (mc.currentScreen is GuiHudDesigner) {
            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.x = 160F

            if (exampleNotification.stayTimer.hasTimePassed(exampleNotification.displayTime)) 
                exampleNotification.stayTimer.reset()

            return getNotifBorder()
        }

        return null
    }

}