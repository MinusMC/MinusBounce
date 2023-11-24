package net.minusmc.minusbounce.ui.client.clickgui.style


abstract class StyleMode(val styleName: String) {
	open fun initGui() {}

	abstract fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float)
	abstract fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)
	open fun handleMouseInput() {}
	abstract fun mouseReleased(mouseX: Int, mouseY: Int, state: Int)
	open fun updateScreen() {}
	open fun onGuiClosed() {}
	open fun keyTyped(typedChar: Char, keyCode: Int)
	open fun doesGuiPauseGame() = false
} 