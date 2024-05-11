package net.minusmc.minusbounce.plugin

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.Listenable

open class Plugin(
	val name: String,
	val version: String,
	val description: String = "No description",
	val authors: Array<String> = arrayOf("No authors"),
	val minApiVersion: PluginAPIVersion,
	val image: String = "",
): Listenable {

	init {
		name.valid("name")
		version.valid("version")
	}

	private fun String?.valid(attr: String) {
		if (this!!.isEmpty() || this.isBlank()) {
			throw MissingInfoPluginException(name, "$name plugin's $attr cannot be empty.")
		}
	}

	fun checkError(): String {
		if (minApiVersion.version < MinusBounce.API_VERSION.version) {
			return "This plugin requires MinusBounce ver ${minApiVersion.id}"
		}
		return ""
	}

	override fun equals(other: Any?) = this === other || (other is Plugin && name == other.name)

	override fun hashCode() = name.hashCode()

	open fun init() {

	}

	open fun registerCommands() {

	}

	open fun registerModules() {

	}

	override fun handleEvents() = true
}

class MissingInfoPluginException(val name: String, message: String): Exception(message)