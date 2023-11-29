package net.minusmc.minusbounce.ui.client.clickgui.styles.other.liquidbounceplus.element

import net.minusmc.minusbounce.ui.font.Fonts
import net.minecraft.client.gui.GuiTextField

class SearchBox(componentId: Int, x: Int, y: Int, width: Int, height: Int): GuiTextField(componentId, Fonts.font40, x, y, width, height) {
    override fun getEnableBackgroundDrawing() = false
}