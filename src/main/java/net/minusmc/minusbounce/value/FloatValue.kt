package net.minusmc.minusbounce.value

import com.google.gson.JsonPrimitive
import com.google.gson.JsonElement

open class FloatValue(name: String, value: Float, val minimum: Float = 0F, val maximum: Float = Float.MAX_VALUE, val suffix: String, displayable: () -> Boolean): Value<Float>(name, value, displayable) {
    constructor(name: String, value: Float, minimum: Float, maximum: Float, displayable: () -> Boolean): this(name, value, minimum, maximum, "", displayable)
    constructor(name: String, value: Float, minimum: Float, maximum: Float, suffix: String): this(name, value, minimum, maximum, suffix, { true } )
    constructor(name: String, value: Float, minimum: Float, maximum: Float): this(name, value, minimum, maximum, { true } )

    fun set(newValue: Number) {
        set(newValue.toFloat())
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asFloat
    }
}