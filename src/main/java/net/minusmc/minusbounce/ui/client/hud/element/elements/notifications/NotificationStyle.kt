package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.ui.client.hud.element.elements.Notifications
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.ui.client.hud.element.Side
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.client.hud.element.Border

abstract class NotificationStyle(val styleName: String, val inst: Notifications): MinecraftInstance() {
	protected val exampleNotification = Notification("Tested", Notification.Type.INFO)
	open val animationY = 30f

	open fun drawStyle(notification: Notification, y: Float) {}

	open fun drawNotifications(notifications: MutableList<Notification>, yPos: Float) {
		var yPos = yPos
		var idx = 0
		for (notification in notifications) {
			notification.drawNotification(yPos, inst)
            if (idx < notifications.size - 1) idx++
            if (inst.side.vertical == Side.Vertical.DOWN)
                yPos += this.animationY
            else 
                yPos -= this.animationY
		}
	}

	open fun drawElement(notifications: MutableList<Notification>): Border? {
        if (mc.currentScreen !is GuiHudDesigner || notifications.isNotEmpty())
            drawNotifications(notifications, 30F)
        else
            exampleNotification.drawNotification(30f, inst)

        if (mc.currentScreen is GuiHudDesigner) {
            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.x = 160F

            if (exampleNotification.stayTimer.hasTimePassed(exampleNotification.displayTime)) 
                exampleNotification.stayTimer.reset()

            return border
        }

        return null
    }

    open val border = Border(0f, 0f, 0f, 0f)

    protected val enumColor = when (type) {
        Type.SUCCESS -> Color(80, 255, 80).rgb
        Type.ERROR -> Color(255, 80, 80).rgb
        Type.INFO -> Color(255, 255, 255).rgb
        Type.WARNING -> Color(255, 255, 0).rgb
    }
}