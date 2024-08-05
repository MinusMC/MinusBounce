package net.minusmc.minusbounce.value

import com.google.gson.JsonPrimitive
import com.google.gson.JsonElement
import java.util.Arrays

open class ListValue(name: String, var values: Array<String>, value: String, displayable: () -> Boolean) : Value<String>(name, value, displayable) {

    constructor(name: String, values: Array<String>, value: String): this(name, values, value, { true } )
    constructor(name: String, values: Array<String>, displayable: () -> Boolean): this(name, values, values[0], displayable)
    constructor(name: String, values: Array<String>): this(name, values, values[0], {true})

    @JvmField
    var openList = false

    init {
        this.value = value
        this.name = name
    }

    operator fun contains(string: String?) = values.any {it.equals(string, true)}

    fun equals(vararg strings: String?) = values.any {value -> strings.any {it.equals(value, true)}}

    override fun changeValue(value: String) {
        for (element in values)
            if (element.equals(value, true)) {
                this.value = element
                break
            }
    }

    fun changeListValues(newValue: Array<String>) {
        this.values = newValue
        this.value = values[0]
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            changeValue(element.asString)
    }
}
