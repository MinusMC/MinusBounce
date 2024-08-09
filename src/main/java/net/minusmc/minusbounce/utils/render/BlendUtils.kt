/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import java.awt.Color

enum class BlendUtils(var colorCode: String) {
    GREEN("§A"),
    GOLD("§6"),
    RED("§C");

    companion object {
        fun getHealthColor(health: Float, maxHealth: Float): Color {
            val fractions = floatArrayOf(0.0f, 0.5f, 1.0f)
            val colors = arrayOf(Color(108, 0, 0), Color(255, 51, 0), Color.GREEN)
            val progress = health / maxHealth
            return blendColors(fractions, colors, progress)!!.brighter()
        }

        fun blendColors(fractions: FloatArray, colors: Array<Color>, progress: Float): Color? {
            return if (fractions.size == colors.size) {
                val indices =
                    getFractionIndices(fractions, progress)
                val range =
                    floatArrayOf(fractions[indices[0]], fractions[indices[1]])
                val colorRange =
                    arrayOf(colors[indices[0]], colors[indices[1]])
                val max = range[1] - range[0]
                val value = progress - range[0]
                val weight = value / max
                blend(colorRange[0], colorRange[1], (1.0f - weight).toDouble())
            } else {
                throw IllegalArgumentException("Fractions and colours must have equal number of elements")
            }
        }

        fun getFractionIndices(fractions: FloatArray, progress: Float): IntArray {
            val range = IntArray(2)
            var startPoint: Int
            startPoint = 0
            while (startPoint < fractions.size && fractions[startPoint] <= progress) {
                ++startPoint
            }
            
            if (startPoint >= fractions.size)
                startPoint = fractions.size - 1

            range[0] = startPoint - 1
            range[1] = startPoint
            return range
        }

        fun blend(color1: Color, color2: Color, ratio: Double): Color {
            val r = ratio.toFloat()
            val ir = 1.0f - r
            val rgb1 = color1.getColorComponents(FloatArray(3))
            val rgb2 = color2.getColorComponents(FloatArray(3))
            var red = rgb1[0] * r + rgb2[0] * ir
            var green = rgb1[1] * r + rgb2[1] * ir
            var blue = rgb1[2] * r + rgb2[2] * ir
            if (red < 0.0f) {
                red = 0.0f
            } else if (red > 255.0f) {
                red = 255.0f
            }
            if (green < 0.0f) {
                green = 0.0f
            } else if (green > 255.0f) {
                green = 255.0f
            }
            if (blue < 0.0f) {
                blue = 0.0f
            } else if (blue > 255.0f) {
                blue = 255.0f
            }
            return Color(red, green, blue)
        }
    }
}
