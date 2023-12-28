/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.client.Animations
import net.minusmc.minusbounce.features.special.MacroManager
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.misc.HttpUtils.get
import net.minusmc.minusbounce.utils.misc.StringUtils
import net.minusmc.minusbounce.utils.render.ColorUtils.translateAlternateColorCodes
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Keyboard

object SettingsUtils {

    /**
     * Execute settings [script]
     */
    fun executeScript(script: String) {
        script.lines().filter { it.isNotEmpty() && !it.startsWith('#') }.forEachIndexed { index, s ->
            val args = s.split(" ").toTypedArray()

            if (args.size <= 1) {
                ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §cSyntax error at line '$index' in setting script.\n§8§lLine: §7$s")
                return@forEachIndexed
            }

            when (args[0]) {
                "chat" -> ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §e${translateAlternateColorCodes(StringUtils.toCompleteString(args, 1))}")
                "unchat" -> ClientUtils.displayChatMessage(translateAlternateColorCodes(StringUtils.toCompleteString(args, 1)))

                "load" -> {
                    val urlRaw = StringUtils.toCompleteString(args, 1)

                    try {
                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §7Loading settings from §a§l$urlRaw§7...")
                        executeScript(get(urlRaw))
                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §7Loaded settings from §a§l$urlRaw§7.")
                    } catch (e: Exception) {
                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §7Failed to load settings from §a§l$urlRaw§7.")
                    }
                }

                "macro" -> {
                    if (args[1] != "0") {
                        val macroBind = args[1]
                        val macroCommand = StringUtils.toCompleteString(args, 2)
                        try {
                            MacroManager.addMacro(macroBind.toInt(), macroCommand)
                            ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] Macro §c§l$macroCommand§7 has been bound to §a§l$macroBind§7.")
                        } catch (e: Exception) {
                            ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §a§l${e.javaClass.name}§7(${e.message}) §cAn Exception occurred while importing macro with keybind §a§l$macroBind§c to §a§l$macroCommand§c.")
                        }
                    }
                }

                else -> {
                    if (args.size < 3) {
                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §cSyntax error at line '$index' in setting script.\n§8§lLine: §7$s")
                        return@forEachIndexed
                    }

                    val moduleName = args[0]
                    val valueName = args[1]
                    val value = args[2]
                    val module = MinusBounce.moduleManager.getModule(moduleName)

                    if (module == null) {
                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §cModule §a§l$moduleName§c was not found!")
                        return@forEachIndexed
                    }

                    if (valueName.equals("toggle", ignoreCase = true)) {
                        module.state = value.equals("true", ignoreCase = true)
                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §a§l${module.name} §7was toggled §c§l${if (module.state) "on" else "off"}§7.")
                        return@forEachIndexed
                    }

                    if (valueName.equals("bind", ignoreCase = true)) {
                        module.keyBind = Keyboard.getKeyIndex(value)
                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §a§l${module.name} §7was bound to §c§l${Keyboard.getKeyName(module.keyBind)}§7.")
                        return@forEachIndexed
                    }

                    val moduleValue = module.getValue(valueName)
                    if (moduleValue == null) {
                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §cValue §a§l$valueName§c don't found in module §a§l$moduleName§c.")
                        return@forEachIndexed
                    }

                    try {
                        when (moduleValue) {
                            is BoolValue -> moduleValue.changeValue(value.toBoolean())
                            is FloatValue -> moduleValue.changeValue(value.toFloat())
                            is IntegerValue -> moduleValue.changeValue(value.toInt())
                            is TextValue -> {
                                val newValue = args.filterIndexed {index, i -> index > 1}.joinToString(" ")
                                moduleValue.changeValue(newValue)
                            }
                            is ListValue -> {
                                LateinitValue.applyValue(valueName, value, moduleName)
                                moduleValue.changeValue(value)
                            }
                            is FontValue -> moduleValue.changeValue(args[2], args[3].toInt())
                            is IntRangeValue -> moduleValue.setForceValue(args[2].toInt(), args[3].toInt())
                            is FloatRangeValue -> moduleValue.setForceValue(args[2].toFloat(), args[3].toFloat())
                        }

                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §a§l${module.name}§7 value §8§l${moduleValue.name}§7 set to §c§l$value§7.")
                    } catch (e: Exception) {
                        ClientUtils.displayChatMessage("§7[§3§lAutoSettings§7] §a§l${e.javaClass.name}§7(${e.message}) §cAn Exception occurred while setting §a§l$value§c to §a§l${moduleValue.name}§c in §a§l${module.name}§c.")
                    }
                }
            }
        }

        MinusBounce.moduleManager.initModeListValues()
        MinusBounce.fileManager.saveConfig(MinusBounce.fileManager.valuesConfig)
    }

    /**
     * Generate settings script
     */
    fun generateScript(): String {
        val stringBuilder = StringBuilder()

        MacroManager.macroMapping.filter { it.key != 0 }.forEach { stringBuilder.append("macro ${it.key} ${it.value}").append("\n") }

        MinusBounce.moduleManager.modules.filter{
            it !is Animations
        }.forEach {
            it.values.forEach { value -> when (value) {
                is IntRangeValue -> stringBuilder.append("${it.name} ${value.name} ${value.get().getMin()} ${value.get().getMax()}").append("\n")
                is FloatRangeValue -> stringBuilder.append("${it.name} ${value.name} ${value.get().getMin()} ${value.get().getMax()}").append("\n")
                is FontValue -> {
                    val fontDetails = Fonts.getFontDetails(value.get())!!
                    stringBuilder.append("${it.name} ${value.name} ${fontDetails[0]} ${fontDetails[1]}").append("\n")
                }
                else -> stringBuilder.append("${it.name} ${value.name} ${value.get()}").append("\n")
            } }

            stringBuilder.append("${it.name} toggle ${it.state}").append("\n")
            stringBuilder.append("${it.name} bind ${Keyboard.getKeyName(it.keyBind)}").append("\n")
        }

        return stringBuilder.toString()
    }

    fun generateDefault(): String {
        val stringBuilder = StringBuilder()
        MacroManager.macroMapping.filter { it.key != 0 }.forEach { stringBuilder.append("macro ${it.key} ${it.value}").append("\n") }

        MinusBounce.moduleManager.modules.filter {
            it !is Animations
        }.forEach {
            it.values.forEach {
                value -> when (value) {
                    is IntRangeValue -> stringBuilder.append("${it.name} ${value.name} ${value.defaultValue.getMin()} ${value.defaultValue.getMax()}").append("\n")
                    is FloatRangeValue -> stringBuilder.append("${it.name} ${value.name} ${value.defaultValue.getMin()} ${value.defaultValue.getMax()}").append("\n")
                    else -> stringBuilder.append("${it.name} ${value.name} ${value.defaultValue}").append("\n")
                }
            }
        }

        return stringBuilder.toString()
    }
}
