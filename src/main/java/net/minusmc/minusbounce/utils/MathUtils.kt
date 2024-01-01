package net.minusmc.minusbounce.utils

import java.math.BigDecimal
import kotlin.math.PI

object MathUtils {

	@JvmStatic
	fun round(f: Float): BigDecimal {
        var bd = BigDecimal(f.toString())
        bd = bd.setScale(2, 4)
        return bd
    }

    fun toRadians(deg: Double): Double = deg / 180.0 * PI
    fun toRadians(deg: Float): Float = deg / 180f * PI.toFloat()
    fun toDegrees(rad: Double): Double = rad * 180.0 / PI
    fun toDegrees(rad: Float): Float = rad * 180f / PI.toFloat()

}