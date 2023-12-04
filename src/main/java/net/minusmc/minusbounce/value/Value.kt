/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.Gson
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.extensions.setAlpha
import net.minecraft.client.gui.FontRenderer
import net.minusmc.minusbounce.utils.FontUtils
import java.awt.Color
import java.util.*

abstract class Value<T>(var name: String, protected var value: T, var canDisplay: () -> Boolean) {
    val defaultValue = value
    val displayableFunction: () -> Boolean
        get() = canDisplay

    fun displayable(func: () -> Boolean): Value<T> {
        canDisplay = func
        return this
    }

    fun get() = value

    fun set(newValue: T) {
        if (newValue == value) return

        val oldValue = get()

        try {
            onChange(oldValue, newValue)
            changeValue(newValue)
            onChanged(oldValue, newValue)
        } catch (e: Exception) {
            ClientUtils.logger.error("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
        }
    }

    open fun changeValue(value: T) {
        this.value = value
    }

    abstract fun toJson(): JsonElement?
    abstract fun fromJson(element: JsonElement)

    protected open fun onChange(oldValue: T, newValue: T) {}
    protected open fun onChanged(oldValue: T, newValue: T) {}

}

/**
 * Bool value represents a value with a boolean
 */
open class BoolValue(name: String, value: Boolean, displayable: () -> Boolean) : Value<Boolean>(name, value, displayable) {

    constructor(name: String, value: Boolean): this(name, value, { true } )

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asBoolean || element.asString.equals("true", ignoreCase = true)
    }

}

/**
 * Integer value represents a value with a integer
 */
open class IntegerValue(name: String, value: Int, val minimum: Int = 0, val maximum: Int = Integer.MAX_VALUE, val suffix: String, displayable: () -> Boolean)
    : Value<Int>(name, value, displayable) {

    constructor(name: String, value: Int, minimum: Int, maximum: Int, displayable: () -> Boolean): this(name, value, minimum, maximum, "", displayable)
    constructor(name: String, value: Int, minimum: Int, maximum: Int, suffix: String): this(name, value, minimum, maximum, suffix, { true } )
    constructor(name: String, value: Int, minimum: Int, maximum: Int): this(name, value, minimum, maximum, { true } )

    fun set(newValue: Number) {
        set(newValue.toInt())
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asInt
    }

}

/**
 * Float value represents a value with a float
 */
open class FloatValue(name: String, value: Float, val minimum: Float = 0F, val maximum: Float = Float.MAX_VALUE, val suffix: String, displayable: () -> Boolean)
    : Value<Float>(name, value, displayable) {

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

/**
 * Text value represents a value with a string
 */
open class TextValue(name: String, value: String, displayable: () -> Boolean) : Value<String>(name, value, displayable) {

    constructor(name: String, value: String): this(name, value, { true } )

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asString
    }
}
/**
 * Font value represents a value with a font
 */
class FontValue(valueName: String, value: FontRenderer, displayable: () -> Boolean) : Value<FontRenderer>(valueName, value, displayable) {

    constructor(valueName: String, value: FontRenderer): this(valueName, value, { true } )

    override fun toJson(): JsonElement? {
        val fontDetails = Fonts.getFontDetails(value) ?: return null
        val valueObject = JsonObject()
        valueObject.addProperty("fontName", fontDetails[0] as String)
        valueObject.addProperty("fontSize", fontDetails[1] as Int)
        return valueObject
    }

    override fun fromJson(element: JsonElement) {
        if (!element.isJsonObject) return
        val valueObject = element.asJsonObject
        value = Fonts.getFontRenderer(valueObject["fontName"].asString, valueObject["fontSize"].asInt)
    }

    fun changeValue(name: String, size: Int) {
        value = Fonts.getFontRenderer(name, size)
    }

    val values
        get() = FontUtils.getAllFontDetails().map { it.second }

    fun setByName(name: String) {
        set((FontUtils.getAllFontDetails().find { it.first.equals(name, true)} ?: return).second )
    }
}

/**
 * Block value represents a value with a block
 */
class BlockValue(name: String, value: Int, displayable: () -> Boolean) : IntegerValue(name, value, 1, 197, displayable) {
    constructor(name: String, value: Int): this(name, value, { true } )
}

/**
 * List value represents a selectable list of values
 */
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

    operator fun contains(string: String?): Boolean {
        return Arrays.stream(values).anyMatch { s: String -> s.equals(string, ignoreCase = true) }
    }

    override fun changeValue(value: String) {
        for (element in values) {
            if (element.equals(value, ignoreCase = true)) {
                this.value = element
                break
            }
        }
    }

    fun changeListValues(newValue: Array<String>) {
        this.values = newValue
        this.value = newValue[0]
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            changeValue(element.asString)
        }
    }
}

abstract class MinMaxRange<T>(protected var minimum: T, protected var maximum: T) {
    fun getMin() = minimum
    fun getMax() = maximum
    fun setMin(value: T) {
        this.minimum = value
    }
    fun setMax(value: T) {
        this.maximum = value
    }
}

class IntRange(minimum: Int, maximum: Int): MinMaxRange<Int>(minimum, maximum)

open class IntRangeValue(name: String, minValue: Int, maxValue: Int, val minimum: Int = 0, val maximum: Int = Int.MAX_VALUE, suffix: String = "", displayable: () -> Boolean): Value<IntRange>(name, IntRange(minValue, maxValue), displayable) {
    constructor(name: String, minValue: Int, maxValue: Int, minimum: Int, maximum: Int, displayable: () -> Boolean): this(name, minValue, maxValue, minimum, maximum, "", displayable)
    constructor(name: String, minValue: Int, maxValue: Int, minimum: Int, maximum: Int, suffix: String): this(name, minValue, maxValue, minimum, maximum, suffix, {true})
    constructor(name: String, minValue: Int, maxValue: Int, minimum: Int, maximum: Int): this(name, minValue, maxValue, minimum, maximum, "", {true})

    fun getMinValue() = value.getMin()
    fun getMaxValue() = value.getMax()

    fun setMinValue(newValue: Number) {
        if (newValue.toInt() <= value.getMax()) value.setMin(newValue.toInt())
    }

    fun setMaxValue(newValue: Number) {
        if (newValue.toInt() >= value.getMin()) value.setMax(newValue.toInt())
    }

    fun changeValue(minValue: Int, maxValue: Int) {
        setMaxValue(maxValue)
        setMinValue(minValue)
    }

    override fun toJson(): JsonElement = Gson().toJsonTree(value)
    override fun fromJson(element: JsonElement) {
        if (element.isJsonObject) {
            changeValue(element.asJsonObject["minimum"].asInt, element.asJsonObject["maximum"].asInt)
        }
    }
}

class FloatRange(minimum: Float, maximum: Float): MinMaxRange<Float>(minimum, maximum)

open class FloatRangeValue(name: String, minValue: Float, maxValue: Float, val minimum: Float = 0f, val maximum: Float = Float.MAX_VALUE, val suffix: String = "", displayable: () -> Boolean): Value<FloatRange>(name, FloatRange(minValue, maxValue), displayable) {
    constructor(name: String, minValue: Float, maxValue: Float, minimum: Float, maximum: Float, displayable: () -> Boolean): this(name, minValue, maxValue, minimum, maximum, "", displayable)
    constructor(name: String, minValue: Float, maxValue: Float, minimum: Float, maximum: Float, suffix: String): this(name, minValue, maxValue, minimum, maximum, suffix, {true})
    constructor(name: String, minValue: Float, maxValue: Float, minimum: Float, maximum: Float): this(name, minValue, maxValue, minimum, maximum, "", {true})

    fun getMinValue() = value.getMin()
    fun getMaxValue() = value.getMax()

    fun setMinValue(newValue: Number) {
        if (newValue.toFloat() <= value.getMax()) value.setMin(newValue.toFloat())
    }

    fun setMaxValue(newValue: Number) {
        if (newValue.toFloat() >= value.getMin()) value.setMax(newValue.toFloat())
    }

    fun changeValue(minValue: Float, maxValue: Float) {
        setMaxValue(maxValue)
        setMinValue(minValue)
    }

    override fun toJson(): JsonElement = Gson().toJsonTree(value)
    override fun fromJson(element: JsonElement) {
        if (element.isJsonObject) {
            changeValue(element.asJsonObject["minimum"].asFloat, element.asJsonObject["maximum"].asFloat)
        }
    }
}
