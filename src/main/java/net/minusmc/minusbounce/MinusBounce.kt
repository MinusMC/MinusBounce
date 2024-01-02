/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce

import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.event.ClientShutdownEvent
import net.minusmc.minusbounce.event.EventManager
import net.minusmc.minusbounce.features.command.CommandManager
import net.minusmc.minusbounce.features.module.ModuleManager
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.features.special.AntiForge
import net.minusmc.minusbounce.features.special.BungeeCordSpoof
import net.minusmc.minusbounce.features.special.CombatManager
import net.minusmc.minusbounce.features.special.MacroManager
import net.minusmc.minusbounce.file.FileManager
import net.minusmc.minusbounce.plugin.PluginAPIVersion
import net.minusmc.minusbounce.plugin.PluginManager
import net.minusmc.minusbounce.ui.client.altmanager.GuiAltManager
import net.minusmc.minusbounce.ui.client.clickgui.styles.StyleMode
import net.minusmc.minusbounce.ui.client.hud.HUD
import net.minusmc.minusbounce.ui.client.hud.HUD.Companion.createDefault
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.misc.sound.TipSoundManager

object MinusBounce {

    // Client information
    const val CLIENT_NAME = "MinusBounce"
    const val CLIENT_FOLDER = "MinusBounce"
    const val CLIENT_VERSION = 20231120
    const val CLIENT_CREATOR = "CCBlueX, MinusMC Team"
    val API_VERSION = PluginAPIVersion.VER_01
    const val CLIENT_CLOUD = "https://minusmc.github.io/MinusCloud/LiquidBounce"
    
    var isStarting = false

    // Managers
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var combatManager: CombatManager
    lateinit var fileManager: FileManager
    lateinit var tipSoundManager: TipSoundManager
    lateinit var pluginManager: PluginManager

    // HUD & ClickGUI
    lateinit var hud: HUD

    // Menu Background
    var background: ResourceLocation? = null

    private var lastTick : Long = 0L

    var playTimeStart: Long = 0L

    val mainMenuButton = hashMapOf<String, Class<out GuiScreen>>()

    fun addMenuButton(name: String, gui: Class<out GuiScreen>) {
        mainMenuButton[name] = gui
    }

    /**
     * Execute if client will be started
     */
    fun startClient() {
        isStarting = true

        ClientUtils.logger.info("Starting $CLIENT_NAME")
        ClassUtils.initCacheClass()
        lastTick = System.currentTimeMillis()
        playTimeStart = System.currentTimeMillis()

        fileManager = FileManager()
        eventManager = EventManager()
        combatManager = CombatManager()
        eventManager.registerListener(RotationUtils)
        eventManager.registerListener(AntiForge())
        eventManager.registerListener(BungeeCordSpoof())
        eventManager.registerListener(InventoryUtils())
        eventManager.registerListener(InventoryHelper)
        eventManager.registerListener(PacketUtils())
        eventManager.registerListener(SessionUtils())
        eventManager.registerListener(MacroManager)
        eventManager.registerListener(combatManager)

        commandManager = CommandManager()
        Fonts.loadFonts()

        tipSoundManager = TipSoundManager()

        moduleManager = ModuleManager()
        moduleManager.registerModules()
        // plugin load modules   

        pluginManager = PluginManager()
        pluginManager.registerPlugins()
        pluginManager.initPlugins()

        pluginManager.registerModules()

        commandManager.registerCommands()
        pluginManager.registerCommands()
        // plugin load command

        fileManager.loadConfigs(fileManager.modulesConfig, fileManager.valuesConfig, fileManager.accountsConfig, fileManager.friendsConfig)

        val clickGuiModule = moduleManager[ClickGUI::class.java]!!
        clickGuiModule.styles = clickGuiModule.styleClazzes.map {it.newInstance() as StyleMode}.sortedBy { it.styleName }

        fileManager.loadConfig(fileManager.clickGuiConfig)

        // Set HUD
        hud = createDefault()
        fileManager.loadConfig(fileManager.hudConfig)

        moduleManager.initModeListValues()

        // Load generators
        GuiAltManager.loadActiveGenerators()

        ClientUtils.logger.info("Finished loading $CLIENT_NAME in ${System.currentTimeMillis() - lastTick}ms.")

        playTimeStart = System.currentTimeMillis()

        // Set is starting status
        isStarting = false
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        eventManager.callEvent(ClientShutdownEvent())

        // Save all available configs
        fileManager.saveAllConfigs()
    }

}