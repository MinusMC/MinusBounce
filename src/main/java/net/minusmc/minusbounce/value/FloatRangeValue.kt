/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.Gson

open class FloatRangeValue(name: String, min: Float, max: Float, val minimum: Float = 0f, val maximum: Float = Float.MAX_VALUE, val suffix: String = "", displayable: () -> Boolean): Value<FloatRange>(name, FloatRange(min, max), displayable) {
    constructor(name: String, min: Float, max: Float, minimum: Float, maximum: Float, displayable: () -> Boolean): this(name, min, max, minimum, maximum, "", displayable)
    constructor(name: String, min: Float, max: Float, minimum: Float, maximum: Float, suffix: String): this(name, min, max, minimum, maximum, suffix, {true})
    constructor(name: String, min: Float, max: Float, minimum: Float, maximum: Float): this(name, min, max, minimum, maximum, "", {true})

    var minValue: Float
        get() = value.minimum
        set(value: Float) {
            if (value <= this.value.maximum)
                this.value.minimum = value
        }

    var maxValue: Float
        get() = value.maximum
        set(value: Float) {
            if (value >= this.value.minimum)
                this.value.maximum = value
        }

    fun setRangeValue(min: Float, max: Float, force: Boolean = false) {
        if (force) {
            this.value.maximum = max
            this.value.minimum = min
        } else if (maxValue < minValue) {
            this.maxValue = minValue
            this.minValue = maxValue
        } else {
            this.maxValue = maxValue
            this.minValue = minValue
        }
    }

    override fun toJson(): JsonElement = Gson().toJsonTree(value)
    override fun fromJson(element: JsonElement) {
        if (element.isJsonObject) {
            val min = element.asJsonObject["minimum"].asFloat
            val max = element.asJsonObject["maximum"].asFloat
            setRangeValue(min, max, true)
        }
    }
}