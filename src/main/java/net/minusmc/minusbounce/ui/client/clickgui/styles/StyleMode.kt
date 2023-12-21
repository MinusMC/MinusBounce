package net.minusmc.minusbounce.ui.client.clickgui.styles

import net.minecraft.client.gui.GuiScreen
import com.google.gson.JsonObject
import com.google.gson.JsonElement


abstract class StyleMode(val styleName: String): GuiScreen() { // wtf wtf
	override fun initGui() {}

	override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
		super.drawScreen(mouseX, mouseY, partialTicks)
	}
	override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
		super.mouseClicked(mouseX, mouseY, mouseButton)
	}
	override fun handleMouseInput() {
		super.handleMouseInput()
	}
	override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
		super.mouseReleased(mouseX, mouseY, state)
	}

	override fun updateScreen() {
		super.updateScreen()
	}
	override fun onGuiClosed() {
		super.onGuiClosed()
	}
	override fun keyTyped(typedChar: Char, keyCode: Int) {
		super.keyTyped(typedChar, keyCode)
	}
	override fun doesGuiPauseGame() = false
} 