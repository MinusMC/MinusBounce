/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.KeyEvent
import net.minusmc.minusbounce.event.Listenable
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.ClassUtils
import java.util.*


class ModuleManager : Listenable {

    val modules = TreeSet<Module> { module1, module2 -> module1.name.compareTo(module2.name) }
    private val moduleClassMap = hashMapOf<Class<*>, Module>()
    var shouldNotify: Boolean = false
    var toggleSoundMode = 0
    var toggleVolume = 0F

    init {
        MinusBounce.eventManager.registerListener(this)
    }

    /**
     * Register all modules
     */
    fun registerModules() {
        ClientUtils.logger.info("[ModuleManager] Loading modules...")
        ClassUtils.resolvePackage("${this.javaClass.`package`.name}.modules", Module::class.java).forEach(this::registerModule)
        modules.forEach {it.onInitialize()}
        ClientUtils.logger.info("[ModuleManager] Successfully loaded ${modules.size} modules.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module) {
        modules += module
        moduleClassMap[module.javaClass] = module

        generateCommand(module)
        MinusBounce.eventManager.registerListener(module)
     }

    /**
     * Register [moduleClass]
     */
    private fun registerModule(moduleClass: Class<out Module>) {
        try {
            registerModule(moduleClass.newInstance())
        } catch (e: IllegalAccessException) {
            registerModule(ClassUtils.getObjectInstance(moduleClass) as Module)
        } catch (e: Throwable) {
            ClientUtils.logger.error("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    fun initModeListValues() {
        modules.forEach {it.onInitModeListValue()}
    }

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module) {
        modules.remove(module)
        moduleClassMap.remove(module::class.java)
        MinusBounce.eventManager.unregisterListener(module)
    }

    /**
     * Generate command for [module]
     */
     fun generateCommand(module: Module) {
        val values = module.values

        if (values.isEmpty())
            return

        MinusBounce.commandManager.registerCommand(ModuleCommand(module, values))
     }

    /**
     * Legacy stuff
     *
     * TODO: Remove later when everything is translated to Kotlin
     */

    /**
     * Get module by [moduleClass]
     */
    fun <T : Module> getModule(moduleClass: Class<T>): T? = moduleClassMap[moduleClass] as T?

    operator fun <T : Module> get(clazz: Class<T>) = getModule(clazz)

    /**
     * Get module by [moduleName]
     */
    fun getModule(moduleName: String?) = modules.find { it.name.equals(moduleName, ignoreCase = true) }

    /**
     * Module related events
     */

    /**
     * Handle incoming key presses
     */
    @EventTarget
    private fun onKey(event: KeyEvent) = modules.filter { it.keyBind == event.key }.forEach { it.toggle() }

    fun getModuleOnCategory(category: ModuleCategory) = modules.filter { it.category == category }

    override fun handleEvents() = true
}
