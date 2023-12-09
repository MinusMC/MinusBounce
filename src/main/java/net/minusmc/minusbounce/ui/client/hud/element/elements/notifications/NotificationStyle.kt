package net.minusmc.minusbounce.ui.client.hud.element.elements.notifications

import net.minusmc.minusbounce.utils.MinecraftInstance

abstract class NotificationStyle(val styleName: String): MinecraftInstance() {
	open fun drawStyle() {}
}