package net.minusmc.minusbounce.value

import com.google.gson.JsonPrimitive
import com.google.gson.JsonElement
import net.minusmc.minusbounce.value.IntegerValue

/**
 * Block value represents a value with a block
 */
open class BlockValue(name: String, value: Int, displayable: () -> Boolean) : IntegerValue(name, value, 1, 197, displayable) {

    var openList = false
    constructor(name: String, value: Int): this(name, value, { true } )
}