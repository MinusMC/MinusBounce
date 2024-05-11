package net.minusmc.minusbounce.plugin

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.Listenable
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.ClientUtils


// TODO: Update API 0.3 and specific Core Plugin file
class PluginManager: Listenable {
	val plugins = mutableListOf<Plugin>()

	init {
		MinusBounce.eventManager.registerListener(this)
	}

	fun registerPlugins() {
		ClassUtils.resolvePlugins().forEach(this::registerPlugin)
	}

	fun initPlugins() {
		plugins.forEach {it.init()}
	}

	fun registerModules() {
		plugins.forEach {it.registerModules()}
	}

	fun registerCommands() {
		plugins.forEach {it.registerCommands()}
	}

	private fun registerPlugin(plugin: Class<out Plugin>) {
		try {
			plugins.add(plugin.newInstance())
		} catch (e: IllegalAccessException) {
			plugins.add(ClassUtils.getObjectInstance(plugin) as Plugin)
		} catch (e: Throwable) {
			ClientUtils.logger.error("Failed to load plugin: ${plugin.name} (${e.javaClass.name}: ${e.message})")
		}
	}

	override fun handleEvents() = true
}