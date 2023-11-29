/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.command.commands

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.command.Command
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.SettingsUtils
import java.io.File
import java.io.IOException

class ConfigCommand: Command("config", arrayOf("settings")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size <= 1) {
            chatSyntax("config <load/save/list/delete>")
            return
        }
        when (args[1].lowercase()) {
            "load" -> loadConfig(args)
            "save" -> saveConfig(args)
            "delete" -> deleteConfig(args)
            "list" -> {
                chat("§cSettings:")
                val settings = this.getLocalSettings() ?: return

                for (file in settings)
                    chat("> " + file.name)
                return
            }
            "create" -> createConfig(args)
        }
    }

    private fun createConfig(args: Array<String>) {
        if (args.size <= 2) {
            chatSyntax("config create <name>")
            return
        }

        val scriptFile = File(MinusBounce.fileManager.settingsDir, args[2])

        try {
            if (scriptFile.exists()) {
                chatSyntax("Config has existed.")
                return
            }
            scriptFile.createNewFile()

            chat("§9Creating config...")
            val settingsScript = SettingsUtils.generateDefault()
            chat("§9Loading config...")
            scriptFile.writeText(settingsScript)
            SettingsUtils.executeScript(settingsScript)
            chat("§6Config created successfully.")
        } catch (throwable: Throwable) {
            chat("§cFailed to create config: §3${throwable.message}")
            ClientUtils.logger.error("Failed to create config.", throwable)
        }
    }

    private fun saveConfig(args: Array<String>) {
        if (args.size <= 2) {
            chatSyntax("config save <name> [all/values/binds/states]...")
            return
        }

        val scriptFile = File(MinusBounce.fileManager.settingsDir, args[2])

        try {
            if (scriptFile.exists())
                if (scriptFile.delete()) {
                    scriptFile.createNewFile()
                    chat("§aSuccessfully deleted old file.")
                } else {
                    chat("§cFailed to delete old file.")
                    return
                }

            chat("§9Creating config...")
            val settingsScript = SettingsUtils.generateScript()
            chat("§9Saving config...")
            scriptFile.writeText(settingsScript)
            chat("§6Config saved successfully.")
        } catch (throwable: Throwable) {
            chat("§cFailed to create config: §3${throwable.message}")
            ClientUtils.logger.error("Failed to create config.", throwable)
        }
    }

    private fun loadConfig(args: Array<String>) {
        if (args.size <= 2) {
            chatSyntax("config load <name>")
            return
        }

        val scriptFile = File(MinusBounce.fileManager.settingsDir, args[2])

        if (scriptFile.exists()) {
            try {
                chat("§9Loading config...")
                val settings = scriptFile.readText()
                chat("§9Set config...")
                SettingsUtils.executeScript(settings)
                chat("§6Config applied successfully.")
                MinusBounce.hud.addNotification(Notification("Config Loaded", Notification.Type.SUCCESS))
                playEdit()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return
        }

        chat("§cConfig file does not exist!")
        
    }

    private fun deleteConfig(args: Array<String>) {
        if (args.size <= 2) {
            chatSyntax("config delete <name>")
            return
        }

        val scriptFile = File(MinusBounce.fileManager.settingsDir, args[2])

        if (scriptFile.exists()) {
            scriptFile.delete()
            chat("§6Config file deleted successfully.")
            return
        }

        chat("§cConfig file does not exist!")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("delete", "list", "load", "save").filter { it.startsWith(args[0], true) }
            2 -> {
                when (args[0].lowercase()) {
                    "delete", "load" -> {
                        val settings = this.getLocalSettings() ?: return emptyList()

                        return settings
                            .map { it.name }
                            .filter { it.startsWith(args[1], true) }
                    }
                }
                return emptyList()
            }
            3 -> {
                if (args[0].equals("save", true)) {
                    return listOf("all", "states", "binds", "values").filter { it.startsWith(args[2], true) }
                }
                return emptyList()
            }
            else -> emptyList()
        }
    }

    private fun getLocalSettings(): Array<File>? = MinusBounce.fileManager.settingsDir.listFiles()
}
