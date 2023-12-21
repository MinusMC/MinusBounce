/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.file

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.file.configs.*
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO


class FileManager: MinecraftInstance() {

    var dir = File(mc.mcDataDir, MinusBounce.CLIENT_FOLDER)
    val fontsDir = File(dir, "fonts")
    val settingsDir = File(dir, "settings")
    val soundsDir = File(dir, "sounds")
    val themesDir = File(dir, "themes")

    val modulesConfig = ModulesConfig(File(dir, "modules.json"))
    val valuesConfig = ValuesConfig(File(dir, "values.json"))
    val clickGuiConfig = ClickGuiConfig(File(dir, "clickgui.json"))
    val accountsConfig = AccountsConfig(File(dir, "accounts.json"))
    val friendsConfig = FriendsConfig(File(dir, "friends.json"))
    val hudConfig = HudConfig(File(dir, "hud.json"))

    private val backgroundFile = File(dir, "userbackground.png")

    companion object {
        val PRETTY_GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    }
    

    /**
     * Constructor of file manager
     * Setup everything important
     */
    init {
        setupFolder()
        loadBackground()
    }

    /**
     * Setup folder
     */
    private fun setupFolder() {
        if(!dir.exists()) dir.mkdir()

        if(!fontsDir.exists()) fontsDir.mkdir()
        if(!settingsDir.exists()) settingsDir.mkdir()
        if(!soundsDir.exists()) soundsDir.mkdir()
        if(!themesDir.exists()) themesDir.mkdir()
    }

    /**
     * Load all configs in file manager
     */
    fun loadAllConfigs() {
        for(field in javaClass.declaredFields) {
            if(field.type == FileConfig::class.java) {
                try {
                    if(!field.isAccessible) field.isAccessible = true
                    val fileConfig = field[this] as FileConfig
                    loadConfig(fileConfig)
                }catch(e: IllegalAccessException) {
                    ClientUtils.logger.error("Failed to load config file of field ${field.name}.", e)
                }
            }
        }
    }

    /**
     * Load a list of configs
     *
     * @param configs list
     */
    fun loadConfigs(vararg configs: FileConfig) {
        for(fileConfig in configs) loadConfig(fileConfig)
    }

    /**
     * Load one config
     *
     * @param config to load
     */
    fun loadConfig(config: FileConfig) {
        if(!config.hasConfig()) {
            ClientUtils.logger.info("[FileManager] Skipped loading config: ${config.file.name}.")
            saveConfig(config, true)
            return
        }

        try {
            config.loadConfig()
            ClientUtils.logger.info("[FileManager] Loaded config: ${config.file.name}.")
        }catch(t: Throwable) {
            ClientUtils.logger.error("[FileManager] Failed to load config file: ${config.file.name}.", t)
        }
    }

    /**
     * Save all configs in file manager
     */
    fun saveAllConfigs() {
        for(field in javaClass.declaredFields) {
            if(field.type == FileConfig::class.java) {
                try {
                    if(!field.isAccessible) field.isAccessible = true
                    val fileConfig = field[this] as FileConfig
                    saveConfig(fileConfig)
                } catch(e: IllegalAccessException) {
                    ClientUtils.logger.error("[FileManager] Failed to save config file of field ${field.name}.", e)
                }
            }
        }
    }

    /**
     * Save a list of configs
     *
     * @param configs list
     */
    fun saveConfigs(vararg configs: FileConfig) {
        for(fileConfig in configs) saveConfig(fileConfig)
    }

    /**
     * Save one config
     *
     * @param config to save
     */
    fun saveConfig(config: FileConfig) {
        saveConfig(config, false)
    }

    /**
     * Save one config
     *
     * @param config         to save
     * @param ignoreStarting check starting
     */
    private fun saveConfig(config: FileConfig, ignoreStarting: Boolean) {
        if (!ignoreStarting && MinusBounce.isStarting) return

        try {
            if(!config.hasConfig()) config.createConfig()
            config.saveConfig()
            ClientUtils.logger.info("[FileManager] Saved config: ${config.file.name}.")
        }catch(t: Throwable) {
            ClientUtils.logger.error("[FileManager] Failed to save config file: ${config.file.name}.", t)
        }
    }

    /**
     * Load background for background
     */
    private fun loadBackground() {
        if(backgroundFile.exists()) {
            try {
                val bufferedImage = ImageIO.read(FileInputStream(backgroundFile)) ?: return
                MinusBounce.background = ResourceLocation(MinusBounce.CLIENT_NAME.lowercase() + "/background.png")
                mc.textureManager.loadTexture(MinusBounce.background, DynamicTexture(bufferedImage))
                ClientUtils.logger.info("[FileManager] Loaded background.")
            } catch (e: Exception) {
                ClientUtils.logger.error("[FileManager] Failed to load background.", e)
            }
        }
    }
}
