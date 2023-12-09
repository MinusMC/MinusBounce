package net.minusmc.minusbounce.utils

import com.google.gson.JsonElement
import net.minusmc.minusbounce.value.ListValue

/*
* Utils that fix value set incorrectly when apply type and mode in speed and fly
* Author: toidicakhia
*/

object LateinitValue {
    var speedModeValue = ""
    var flyModeValue = ""

    fun applyValue(moduleValue: ListValue, value: JsonElement, moduleName: String) {
        if (moduleName.equals("Speed", true) && moduleValue.name.equals("Mode", true)) {
            speedModeValue = value.asString
        }

        if (moduleName.equals("Fly", true) && moduleValue.name.equals("Mode", true)) {
            flyModeValue = value.asString
        }
    }

    fun applyValue(valueName: String, value: String, moduleName: String) {
        if (moduleName.equals("Speed", true) && valueName.equals("Mode", true)) {
            speedModeValue = value
        }

        if (moduleName.equals("Fly", true) && valueName.equals("Mode", true)) {
            flyModeValue = value
        }
    }
}