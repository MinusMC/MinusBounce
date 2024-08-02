/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.Gson

open class IntRangeValue(name: String, min: Int, max: Int, val minimum: Int = 0, val maximum: Int = Int.MAX_VALUE, val suffix: String = "", displayable: () -> Boolean): Value<IntRange>(name, IntRange(min, max), displayable) {
    constructor(name: String, min: Int, max: Int, minimum: Int, maximum: Int, displayable: () -> Boolean): this(name, min, max, minimum, maximum, "", displayable)
    constructor(name: String, min: Int, max: Int, minimum: Int, maximum: Int, suffix: String): this(name, min, max, minimum, maximum, suffix, {true})
    constructor(name: String, min: Int, max: Int, minimum: Int, maximum: Int): this(name, min, max, minimum, maximum, "", {true})

    var minValue: Int
        get() = value.minimum
        set(value: Int) {
            if (value <= this.value.maximum)
                this.value.minimum = value
        }

    var maxValue: Int
        get() = value.maximum
        set(value: Int) {
            if (value >= this.value.minimum)
                this.value.maximum = value
        }

    fun setRangeValue(min: Int, max: Int, force: Boolean = false) {
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
            val min = element.asJsonObject["minimum"].asInt
            val max = element.asJsonObject["maximum"].asInt
            setRangeValue(min, max, true)
        }
    }
}