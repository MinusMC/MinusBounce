/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import kotlin.math.*

/***
 * Thanks https://github.com/ai/easings.net for this util, converted to Kotlin.
 */
object EaseUtils {
    @JvmStatic
    fun easeInSine(x: Double) = 1 - cos((x * PI) / 2)

    @JvmStatic
    fun easeOutSine(x: Double) = sin((x * PI) / 2)

    @JvmStatic
    fun easeInOutSine(x: Double) = -(cos(PI * x) - 1) / 2

    @JvmStatic
    fun easeInQuad(x: Double) = x * x

    @JvmStatic
    fun easeOutQuad(x: Double) = 1 - easeInQuad(1 - x)

    @JvmStatic
    fun easeInOutQuad(x: Double) = if (x < 0.5) 2 * x * x else 1 - easeInQuad(-2 * x + 2) / 2 

    @JvmStatic
    fun easeInCubic(x: Double) = x * x * x

    @JvmStatic
    fun easeOutCubic(x: Double) = 1 - easeInCubic(x)

    @JvmStatic
    fun easeInOutCubic(x: Double) = if (x < 0.5) 4 * x * x * x else 1 - easeInCubic(-2 * x + 2) / 2 

    @JvmStatic
    fun easeInQuart(x: Double) = x * x * x * x

    @JvmStatic
    fun easeOutQuart(x: Double) = 1 - easeInQuart(x)

    @JvmStatic
    fun easeInOutQuart(x: Double) = if (x < 0.5) 8 * x * x * x * x else 1 - easeInQuart(-2 * x + 2) / 2

    @JvmStatic
    fun easeInQuint(x: Double) = x * x * x * x * x

    @JvmStatic
    fun easeOutQuint(x: Double) = 1 - easeInQuint(x)

    @JvmStatic
    fun easeInOutQuint(x: Double) = if (x < 0.5) 16 * x * x * x * x * x else 1 - easeOutQuint(-2 * x + 2) / 2

    @JvmStatic
    fun easeInExpo(x: Double) = if(x == 0.0) 0.0 else 2.0.pow(10 * x - 10)

    @JvmStatic
    fun easeOutExpo(x: Double) = if(x == 1.0) 1.0 else 1 - 2.0.pow(-10 * x)

    @JvmStatic
    fun easeInOutExpo(x: Double) = if(x == 0.0 || x == 1.0) x
        else if (x < 0.5) 2.0.pow(20 * x - 10) / 2
        else (2 - 2.0.pow(-20 * x + 10)) / 2

    @JvmStatic
    fun easeInCirc(x: Double) = 1 - sqrt(1 - x.pow(2))

    @JvmStatic
    fun easeOutCirc(x: Double) = sqrt(1 - (x - 1).pow(2))

    @JvmStatic
    fun easeInOutCirc(x: Double) = if (x < 0.5) (1 - sqrt(1 - (2 * x).pow(2))) / 2
        else (sqrt(1 - (-2 * x + 2).pow(2)) + 1) / 2

    @JvmStatic
    fun easeInBack(x: Double) = 2.70158 * x * x * x - 1.70158 * x * x

    @JvmStatic
    fun easeOutBack(x: Double) = 3.70158 * (x - 1).pow(3) + 1.70158 * (x - 1).pow(2)

    @JvmStatic
    fun easeInOutBack(x: Double) = if (x < 0.5) ((2 * x).pow(2) * (7.189819 * x - 2.5949095)) / 2
        else ((2 * x - 2).pow(2) * (3.5949095 * (x * 2 - 2) + 2.5949095) + 2) / 2

    @JvmStatic
    fun easeInElastic(x: Double) = if (x == 0.0 || x == 1.0) x
        else (-2.0).pow(10 * x - 10) * sin((x * 10 - 10.75) * 2.0943951023931953)

    @JvmStatic
    fun easeOutElastic(x: Double) = if (x == 0.0 || x == 1.0) x
            else 2.0.pow(-10 * x) * sin((x * 10 - 0.75) * 2.0943951023931953) + 1

    @JvmStatic
    fun easeInOutElastic(x: Double) = if (x == 0.0 || x == 1.0) x
        else if (x < 0.5) -(2.0.pow(20 * x - 10) * sin((20 * x - 11.125) * 1.3962634015954636)) / 2
        else (2.0.pow(-20 * x + 10) * sin((20 * x - 11.125) * 1.3962634015954636)) / 2 + 1

    @JvmStatic
    fun easeInBounce(x: Double) = 1 - easeOutBounce(1 - x)

    @JvmStatic
    fun easeOutBounce(animeX: Double) = if (animeX * 2.75 < 1) 7.5625 * animeX * animeX
        else if (animeX * 2.75 < 2) 7.5625 * ((animeX - 1.5) / 2.75) * (animeX - 1.5) + 0.75
        else if (animeX * 2.75 < 2.5) 7.5625 * ((animeX - 2.25) / 2.75) * (animeX - 2.25) + 0.9375
        else 7.5625 * ((animeX - 2.625) / 2.75) * (animeX - 2.625) + 0.984375

    @JvmStatic
    fun easeInOutBounce(x: Double) = if (x < 0.5) (1 - easeOutBounce(1 - 2 * x)) / 2 
        else (1 + easeOutBounce(2 * x - 1)) / 2
}
