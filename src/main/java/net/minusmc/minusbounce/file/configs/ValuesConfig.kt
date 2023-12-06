/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.file.configs

import com.google.gson.*
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.special.AntiForge
import net.minusmc.minusbounce.features.special.AutoReconnect.delay
import net.minusmc.minusbounce.features.special.BungeeCordSpoof
import net.minusmc.minusbounce.features.special.MacroManager.addMacro
import net.minusmc.minusbounce.features.special.MacroManager.macroMapping
import net.minusmc.minusbounce.file.FileConfig
import net.minusmc.minusbounce.file.FileManager
import net.minusmc.minusbounce.ui.client.GuiBackground.Companion.enabled
import net.minusmc.minusbounce.ui.client.GuiBackground.Companion.particles
import net.minusmc.minusbounce.ui.client.altmanager.menus.GuiTheAltening.Companion.apiKey
import net.minusmc.minusbounce.utils.LateinitValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.Value
import java.io.*
import java.util.function.Consumer

class ValuesConfig(file: File?) : FileConfig(file!!) {
    @Throws(IOException::class)
    override fun loadConfig() {
        val jsonElement = JsonParser().parse(BufferedReader(FileReader(file)))
        if (jsonElement is JsonNull) return
        val jsonObject = jsonElement as JsonObject
        val iterator: Iterator<Map.Entry<String, JsonElement>> = jsonObject.entrySet().iterator()
        while (iterator.hasNext()) {
            val (key, value) = iterator.next()
            if (key.equals("CommandPrefix", ignoreCase = true)) {
                MinusBounce.commandManager.prefix = value.asCharacter
            } else if (key.equals("macros", ignoreCase = true)) {
                val jsonValue = value.getAsJsonArray()
                for (macroElement in jsonValue) {
                    val macroObject = macroElement.getAsJsonObject()
                    val keyValue = macroObject["key"]
                    val commandValue = macroObject["command"]
                    addMacro(keyValue.asInt, commandValue.asString)
                }
            } else if (key.equals("features", ignoreCase = true)) {
                val jsonValue = value as JsonObject
                if (jsonValue.has("AntiForge")) AntiForge.enabled = jsonValue["AntiForge"].asBoolean
                if (jsonValue.has("AntiForgeFML")) AntiForge.blockFML = jsonValue["AntiForgeFML"].asBoolean
                if (jsonValue.has("AntiForgeProxy")) AntiForge.blockProxyPacket = jsonValue["AntiForgeProxy"].asBoolean
                if (jsonValue.has("AntiForgePayloads")) AntiForge.blockPayloadPackets =
                    jsonValue["AntiForgePayloads"].asBoolean
                if (jsonValue.has("BungeeSpoof")) BungeeCordSpoof.enabled = jsonValue["BungeeSpoof"].asBoolean
                if (jsonValue.has("AutoReconnectDelay")) delay = jsonValue["AutoReconnectDelay"].asInt
            } else if (key.equals("thealtening", ignoreCase = true)) {
                val jsonValue = value as JsonObject
                if (jsonValue.has("API-Key")) apiKey = jsonValue["API-Key"].asString
            } else if (key.equals("Background", ignoreCase = true)) {
                val jsonValue = value as JsonObject
                if (jsonValue.has("Enabled")) enabled = jsonValue["Enabled"].asBoolean
                if (jsonValue.has("Particles")) particles = jsonValue["Particles"].asBoolean
            } else {
                val module = MinusBounce.moduleManager.getModule(key)
                if (module != null) {
                    val jsonModule = value as JsonObject
                    for (moduleValue in module.values) {
                        val element = jsonModule[moduleValue.name]
                        if (element != null) {
                            moduleValue.fromJson(element)
                            if (moduleValue is ListValue)
                                LateinitValue.applyValue(moduleValue, element, key)
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun saveConfig() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("CommandPrefix", MinusBounce.commandManager.prefix)
        val jsonMacros = JsonArray()
        macroMapping.forEach { (k: Int?, v: String?) ->
            val jsonMacro = JsonObject()
            jsonMacro.addProperty("key", k)
            jsonMacro.addProperty("command", v)
            jsonMacros.add(jsonMacro)
        }
        jsonObject.add("macros", jsonMacros)
        val jsonFeatures = JsonObject()
        jsonFeatures.addProperty("AntiForge", AntiForge.enabled)
        jsonFeatures.addProperty("AntiForgeFML", AntiForge.blockFML)
        jsonFeatures.addProperty("AntiForgeProxy", AntiForge.blockProxyPacket)
        jsonFeatures.addProperty("AntiForgePayloads", AntiForge.blockPayloadPackets)
        jsonFeatures.addProperty("BungeeSpoof", BungeeCordSpoof.enabled)
        jsonFeatures.addProperty("AutoReconnectDelay", delay)
        jsonObject.add("features", jsonFeatures)
        val theAlteningObject = JsonObject()
        theAlteningObject.addProperty("API-Key", apiKey)
        jsonObject.add("thealtening", theAlteningObject)
        val backgroundObject = JsonObject()
        backgroundObject.addProperty("Enabled", enabled)
        backgroundObject.addProperty("Particles", particles)
        jsonObject.add("Background", backgroundObject)
        MinusBounce.moduleManager.modules.stream().filter { module: Module -> module.values.isNotEmpty() }
            .forEach { module: Module ->
                val jsonModule = JsonObject()
                module.values.forEach(Consumer { value: Value<*> ->
                    jsonModule.add(value.name, value.toJson())
                })
                jsonObject.add(module.name, jsonModule)
            }
        val printWriter = PrintWriter(FileWriter(file))
        printWriter.println(FileManager.PRETTY_GSON.toJson(jsonObject))
        printWriter.close()
    }
}