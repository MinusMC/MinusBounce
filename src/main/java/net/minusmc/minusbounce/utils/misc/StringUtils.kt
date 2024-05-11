/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.misc

import java.util.*

object StringUtils {
    private val stringCache = HashMap<String, String>()
    private val stringReplaceCache = HashMap<String, String>()
    private val stringRegexCache = HashMap<String, String?>()
    private val airCache = HashMap<String, String>()
    
    fun fixString(str: String): String? {
        var str = str
        if (stringCache.containsKey(str)) return stringCache[str]
        str = str.replace("\uF8FF".toRegex(), "") //remove air chars
        val sb = StringBuilder()
        for (c in str.toCharArray()) {
            if (c.code > 33 + 65248 && c.code < 128 + 65248) {
                sb.append(Character.toChars(c.code - 65248))
            } else {
                sb.append(c)
            }
        }
        val result = sb.toString()
        stringCache[str] = result
        return result
    }

    fun injectAirString(str: String): String? {
        if (airCache.containsKey(str)) return airCache[str]
        val stringBuilder = StringBuilder()
        var hasAdded = false
        for (c in str.toCharArray()) {
            stringBuilder.append(c)
            if (!hasAdded) stringBuilder.append('\uF8FF')
            hasAdded = true
        }
        val result = stringBuilder.toString()
        airCache[str] = result
        return result
    }

    fun toCompleteString(args: Array<String>, start: Int): String {
        return if (args.size <= start) "" else java.lang.String.join(" ", *Arrays.copyOfRange(args, start, args.size))
    }

    @JvmOverloads
    fun replace(string: String, searchChars: String, replaceChars: String?, forceReload: Boolean = false): String {
        var replaceChars = replaceChars
        if (string.isEmpty() || searchChars.isEmpty() || searchChars == replaceChars) return string
        if (!forceReload && stringRegexCache[searchChars] != null && stringRegexCache[searchChars] == replaceChars && stringReplaceCache.containsKey(
                string
            )
        ) return stringReplaceCache.getOrDefault(
            string,
            replace(string, searchChars, replaceChars, true)
        ) // will attempt to retry replacement once again
        if (replaceChars == null) replaceChars = ""
        val stringLength = string.length
        val searchCharsLength = searchChars.length
        val stringBuilder = StringBuilder(string)
        for (i in 0 until stringLength) {
            val start = stringBuilder.indexOf(searchChars, i)
            if (start == -1) {
                return if (i == 0) string else stringBuilder.toString()
            }
            stringBuilder.replace(start, start + searchCharsLength, replaceChars)
        }
        val result = stringBuilder.toString()
        stringReplaceCache[string] = result
        stringRegexCache[searchChars] = replaceChars
        return result
    }

    fun getFormatTime(deltaTime: Long): String {
        val realTime = deltaTime / 1000

        val hours = realTime / 3600
        val minutes = (realTime % 3600) / 60
        val seconds = (realTime % 3600) % 60

        return "${hours}h ${minutes}m ${seconds}s"
    }
}
