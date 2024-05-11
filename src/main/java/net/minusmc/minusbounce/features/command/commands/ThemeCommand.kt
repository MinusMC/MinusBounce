/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.command.commands

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.command.Command
import net.minusmc.minusbounce.ui.client.hud.Config
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.utils.ClientUtils
import java.io.File
import java.io.IOException

class ThemeCommand : Command("theme", arrayOf("thememanager", "tm", "themes")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size <= 1) {
            chatSyntax("theme <load/save/list/delete>")
            return
        }

        when (args[1].lowercase()) {
            "load" -> {
                if (args.size > 2) {
                    val themeFile = File(MinusBounce.fileManager.themesDir, args[2])

                    if (themeFile.exists()) {
                        try {
                            chat("§9Loading theme...")
                            val theme = themeFile.readText()
                            chat("§9Set theme settings...")
                            MinusBounce.isStarting = true
                            MinusBounce.hud.clearElements()
                            MinusBounce.hud = Config(theme).toHUD()
                            MinusBounce.isStarting = false
                            chat("§6Theme applied successfully.")
                            MinusBounce.hud.addNotification(Notification("Theme loaded", args[2], Notification.Type.SUCCESS))
                            playEdit()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        return
                    }

                    chat("§cTheme file does not exist!")
                    return
                }

                chatSyntax("theme load <name>")
                return
            }

            "save" -> {
                if (args.size > 2) {
                    val themeFile = File(MinusBounce.fileManager.themesDir, args[2])

                    try {
                        if (themeFile.exists())
                            themeFile.delete()
                        themeFile.createNewFile()

                        chat("§9Creating theme settings...")
                        val settingsTheme = Config(MinusBounce.hud).toJson()
                        chat("§9Saving theme...")
                        themeFile.writeText(settingsTheme)
                        chat("§6Theme saved successfully.")
                    } catch (throwable: Throwable) {
                        chat("§cFailed to create local theme config: §3${throwable.message}")
                        ClientUtils.logger.error("Failed to create local theme config.", throwable)
                    }
                    return
                }

                chatSyntax("theme save <name>")
                return
            }

            "delete" -> {
                if (args.size > 2) {
                    val themeFile = File(MinusBounce.fileManager.themesDir, args[2])

                    if (themeFile.exists()) {
                        themeFile.delete()
                        chat("§6Theme file deleted successfully.")
                        return
                    }

                    chat("§cTheme file does not exist!")
                    return
                }

                chatSyntax("theme delete <name>")
                return
            }

            "list" -> {
                chat("§cThemes:")

                val themes = this.getLocalThemes() ?: return

                for (file in themes)
                    chat("> " + file.name)
                return
            }
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("delete", "list", "load", "save").filter { it.startsWith(args[0], true) }
            2 -> {
                when (args[0].lowercase()) {
                    "delete", "load" -> {
                        val settings = this.getLocalThemes() ?: return emptyList()

                        return settings
                            .map { it.name }
                            .filter { it.startsWith(args[1], true) }
                    }
                }
                return emptyList()
            }
            else -> emptyList()
        }
    }

    private fun getLocalThemes(): Array<File>? = MinusBounce.fileManager.themesDir.listFiles()
}
