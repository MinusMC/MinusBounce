package net.minusmc.minusbounce.ui.client.clickgui.styles.other.liquidbounceplus.element.module.value

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value

import java.awt.Color

abstract class ValueElement<T>(val value: Value<T>) : MinecraftInstance() {

    var valueHeight = 20F

    abstract fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, bgColor: Color, accentColor: Color): Float
    abstract fun onClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float)
    open fun onRelease(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {}

    open fun onKeyPress(typed: Char, keyCode: Int): Boolean = false

    fun isDisplayable(): Boolean = value.canDisplay()
}