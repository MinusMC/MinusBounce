package net.minusmc.minusbounce.utils.geometry

data class Rectagle(var x: Float, var y: Float, var x2: Float, var y2: Float) {
	constructor(x: Number, y: Number, x2: Number, y2: Number): this(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat())

	fun isMouseHover(mouseX: Float, mouseY: Float): Boolean {
		return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2
	}

	fun isMouseHover(mouseX: Number, mouseY: Number) = isMouseHover(mouseX.toFloat(), mouseY.toFloat())
}