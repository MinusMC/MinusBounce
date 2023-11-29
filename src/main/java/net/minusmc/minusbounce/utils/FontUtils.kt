package net.minusmc.minusbounce.utils

import net.minusmc.minusbounce.ui.font.Fonts
import net.minecraft.client.gui.FontRenderer

object FontUtils {
    private val cache: MutableList<Pair<String, FontRenderer>> = mutableListOf()

    private fun updateCache() {
        cache.clear()
        for (fontOfFonts in Fonts.fonts) {
            val details = Fonts.getFontDetails(fontOfFonts) ?: continue
            val name = details[0].toString()
            val size = details[1].toString().toInt()
            val format = "$name $size"

            cache.add(format to fontOfFonts)
        }

        cache.sortBy { it.first }
    }

    fun getAllFontDetails(): Array<Pair<String, FontRenderer>> {
        if (cache.size == 0) updateCache()

        return cache.toTypedArray()
    }
}

fun FontRenderer.getName() = FontUtils.getAllFontDetails().find { it.second == this }!!.first