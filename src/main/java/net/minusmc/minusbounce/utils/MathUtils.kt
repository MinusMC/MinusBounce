package net.minusmc.minusbounce.utils

import java.math.BigDecimal

object MathUtils {

	@JvmStatic
	fun round(f: Float): BigDecimal {
        var bd = BigDecimal(f.toString())
        bd = bd.setScale(2, 4)
        return bd
    }

}