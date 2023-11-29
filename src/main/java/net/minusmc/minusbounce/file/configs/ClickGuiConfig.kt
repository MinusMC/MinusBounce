/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.file.configs

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.file.FileConfig
import net.minusmc.minusbounce.file.FileManager
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.ModuleElement
import net.minusmc.minusbounce.ui.client.clickgui.DropDownClickGui
import net.minusmc.minusbounce.utils.ClientUtils
import java.io.*

class ClickGuiConfig(file: File?) : FileConfig(file!!) {
    override fun loadConfig() {
        val jsonElement = JsonParser().parse(BufferedReader(FileReader(file)))
        if (jsonElement is JsonNull) return
        val jsonObject = jsonElement as JsonObject
        val clickGui = MinusBounce.moduleManager[ClickGUI::class.java]!!.style
        if (clickGui !is DropDownClickGui) return
        for (panel in clickGui.panels) {
            if (!jsonObject.has(panel.name)) continue
            try {
                val panelObject = jsonObject.getAsJsonObject(panel.name)
                panel.open = panelObject["open"].asBoolean
                panel.isVisible = panelObject["visible"].asBoolean
                panel.x = panelObject["posX"].asInt
                panel.y = panelObject["posY"].asInt
                for (element in panel.elements) {
                    if (element !is ModuleElement) continue
                    if (!panelObject.has(element.module.name)) continue
                    try {
                        val elementObject = panelObject.getAsJsonObject(element.module.name)
                        element.isShowSettings = elementObject["Settings"].asBoolean
                    } catch (e: Exception) {
                        ClientUtils.logger.error(
                            "Error while loading clickgui module element with the name '" + element.module.name + "' (Panel Name: " + panel.name + ").",
                            e
                        )
                    }
                }
            } catch (e: Exception) {
                ClientUtils.logger
                    .error("Error while loading clickgui panel with the name '" + panel.name + "'.", e)
            }
        }
    }

    override fun saveConfig() {
        val jsonObject = JsonObject()
        val clickGui = MinusBounce.moduleManager[ClickGUI::class.java]!!.style
        if (clickGui !is DropDownClickGui) return
        for (panel in clickGui.panels) {
            val panelObject = JsonObject()
            panelObject.addProperty("open", panel.open)
            panelObject.addProperty("visible", panel.isVisible)
            panelObject.addProperty("posX", panel.x)
            panelObject.addProperty("posY", panel.y)
            for (element in panel.elements) {
                if (element !is ModuleElement) continue
                val elementObject = JsonObject()
                elementObject.addProperty("Settings", element.isShowSettings)
                panelObject.add(element.module.name, elementObject)
            }
            jsonObject.add(panel.name, panelObject)
        }
        val printWriter = PrintWriter(FileWriter(file))
        printWriter.println(FileManager.PRETTY_GSON.toJson(jsonObject))
        printWriter.close()
    }
}