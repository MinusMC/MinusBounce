/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.dropdown.elements

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.ui.client.clickgui.dropdown.DropDownClickGui

open class ButtonElement(var displayName: String?) : Element() {
    var color = 0xffffff
    var hoverTime = 0

    override fun drawScreen(mouseX: Int, mouseY: Int, button: Float) {
        MinusBounce.clickGui.drawButtonElement(mouseX, mouseY, this)
        super.drawScreen(mouseX, mouseY, button)
    }

    override var height: Int
        get() = 16
        set(height) {
            super.height = height
        }

    fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16
    }
}