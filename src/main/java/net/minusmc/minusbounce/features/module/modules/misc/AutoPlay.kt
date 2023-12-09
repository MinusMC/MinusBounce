/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.misc

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.WorldEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue


@ModuleInfo(name = "AutoPlay", spacedName = "Auto Play", description = "Automatically move you to another game after finishing it.", category = ModuleCategory.MISC)
class AutoPlay : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.autoplays", AutoPlayMode::class.java)
        .map { it.newInstance() as AutoPlayMode }
        .sortedBy { it.modeName }

    private val mode: AutoPlayMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Redesky") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    val delayValue = IntegerValue("JoinDelay", 3, 0, 7, " seconds")

    override fun onEnable() {
        mode.onEnable()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        mode.onWorld()
    }

    override val tag: String
        get() = modeValue.get()
}