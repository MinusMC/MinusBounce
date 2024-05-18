/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.value

import com.google.gson.JsonElement
import net.minusmc.minusbounce.utils.ClientUtils

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
            onPreChange(oldValue, newValue)
            changeValue(newValue)
            onPostChange(oldValue, newValue)
        } catch (e: Exception) {
            ClientUtils.logger.error("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
        }
    }

    open fun changeValue(value: T) {
        this.value = value
    }

    abstract fun toJson(): JsonElement?
    abstract fun fromJson(element: JsonElement)

    protected open fun onPreChange(oldValue: T, newValue: T) {}
    protected open fun onPostChange(oldValue: T, newValue: T) {}

}