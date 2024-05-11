package net.minusmc.minusbounce.utils.player


data class MovementSpeed(val forward: Float, val strafe: Float, val yaw: Float) {
    constructor(forward: Double, strafe: Double): this(forward.toFloat(), strafe.toFloat(), 0f)
    constructor(forward: Double, strafe: Double, yaw: Double): this(forward.toFloat(), strafe.toFloat(), yaw.toFloat())
    constructor(forward: Float, strafe: Float): this(forward, strafe, 0f)

    override fun toString(): String {
        return "MovementSpeed(yaw=$yaw, forward=$forward, strafe=$strafe)"
    }
}