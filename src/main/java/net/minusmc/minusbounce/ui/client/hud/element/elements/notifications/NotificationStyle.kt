package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

abstract class NotificationStyle(val styleName: String): MinecraftInstance() {
	open fun drawStyle() {}
}