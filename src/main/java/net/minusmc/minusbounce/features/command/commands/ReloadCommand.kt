/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.command.commands

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.ReloadClientEvent
import net.minusmc.minusbounce.features.command.Command
import net.minusmc.minusbounce.features.command.CommandManager
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.misc.sound.TipSoundManager

class ReloadCommand : Command("reload", arrayOf("configreload")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        chat("Reloading...")
        chat("§c§lReloading commands...")
        MinusBounce.commandManager = CommandManager()
        MinusBounce.commandManager.registerCommands()
        MinusBounce.isStarting = true
        for(module in MinusBounce.moduleManager.modules)
            MinusBounce.moduleManager.generateCommand(module)
        chat("§c§lReloading fonts...")
        Fonts.loadFonts()
        chat("§c§lReloading toggle audio files...")
        MinusBounce.tipSoundManager = TipSoundManager()
        chat("§c§lReloading modules...")
        MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.modulesConfig)
        MinusBounce.isStarting = false
        chat("§c§lReloading values...")
        MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.valuesConfig)
        chat("§c§lReloading accounts...")
        MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.accountsConfig)

        // call reload command
        MinusBounce.eventManager.callEvent(ReloadClientEvent())

        chat("§c§lReloading HUD...")
        MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.hudConfig)
        chat("§c§lReloading ClickGUI...")
        MinusBounce.moduleManager[ClickGUI::class.java]!!.style::class.java.newInstance()
        MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.clickGuiConfig)

        MinusBounce.moduleManager.initModeListValues()

        MinusBounce.isStarting = false
        chat("Reloaded.")
    }
}
