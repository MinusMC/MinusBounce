/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.misc

import kotlin.random.Random

/**
 * Utils for random
 */

object RandomUtils {
    fun nextBoolean(): Boolean = Random.nextBoolean()

    fun nextInt(startInclusive: Int, endExclusive: Int): Int {
        return if (endExclusive - startInclusive <= 0) startInclusive 
            else startInclusive + Random.nextInt(endExclusive - startInclusive)
    }

    fun nextDouble(startInclusive: Double, endInclusive: Double): Double {
        return if (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0) startInclusive
            else startInclusive + Random.nextDouble(endInclusive - startInclusive)
    }

    fun nextFloat(startInclusive: Float, endInclusive: Float): Float {
        return nextDouble(startInclusive.toDouble(), endInclusive.toDouble()).toFloat()
    }

    fun randomNumber(length: Int): String {
        return random(length, "0123456789")
    }

    fun randomString(length: Int): String {
        return random(length, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
    }

    fun random(length: Int, chars: String): String = (1..length).map {
        chars.random()
    }.joinToString("")

    fun randomDelay(minDelay: Int, maxDelay: Int): Long {
        return nextInt(minDelay, maxDelay).toLong()
    }

    fun randomClickDelay(minCPS: Int, maxCPS: Int): Long {
        return (Random.nextDouble() * (1000 / minCPS - 1000 / maxCPS + 1) + 1000 / maxCPS).toLong()
    }
}
