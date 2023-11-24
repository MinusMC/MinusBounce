package net.minusmc.minusbounce.ui.client.clickgui

import net.minusmc.minusbounce.value.*

interface DropDownClickGui {
	var mouseDown = false
    var rightMouseDown = false

    var yPos = 0

    fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel?)
    fun drawDescription(mouseX: Int, mouseY: Int, text: String?)
    fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement?)
    fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement?)


    fun drawValues(moduleElement: ModuleElement)
    fun drawBoolValue(value: BoolValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    fun drawListValue(value: ListValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    fun drawFloatValue(value: FloatValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    fun drawIntegerValue(value: IntergerValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    fun drawFontValue(value: FontValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    fun drawTextValue(value: TextValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    fun drawIntRangeValue(value: IntRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
    fun drawFloatRangeValue(value: FloatRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int)
}