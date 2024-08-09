package net.minusmc.minusbounce.value

import com.google.gson.JsonPrimitive
import com.google.gson.JsonElement

open class BoolValue(name: String, value: Boolean, displayable: () -> Boolean) : Value<Boolean>(name, value, displayable) {

    constructor(name: String, value: Boolean): this(name, value, { true } )

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asBoolean || element.asString.equals("true", ignoreCase = true)
    }

}