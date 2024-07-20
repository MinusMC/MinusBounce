package net.minusmc.minusbounce.utils.misc

data class MotionData(var motionX: Double, var motionY: Double, var motionZ: Double) {
	constructor(motionX: Double, motionZ: Double): this(motionX, 0.0, motionZ)
}