/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements

import net.minusmc.minusbounce.utils.MinecraftInstance

open class Element : MinecraftInstance() {
    var x = 0
    var y = 0
    var width = 0
    open var height = 0
    var isVisible = false

    fun setLocation(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    open fun drawScreen(mouseX: Int, mouseY: Int, button: Float) {}
    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int): Boolean {
        return false
    }
}
