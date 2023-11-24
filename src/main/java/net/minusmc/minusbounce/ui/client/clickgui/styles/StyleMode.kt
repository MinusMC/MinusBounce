package net.minusmc.minusbounce.ui.client.clickgui.styles

import net.minecraft.client.gui.GuiScreen


abstract class StyleMode(val styleName: String): GuiScreen() {
	override fun initGui() {}

	override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}
	override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {}
	override fun handleMouseInput() {}
	override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
	override fun updateScreen() {}
	override fun onGuiClosed() {}
	override fun keyTyped(typedChar: Char, keyCode: Int) {}
	override fun doesGuiPauseGame() = false
} 