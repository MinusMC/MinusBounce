/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.MoveInputEvent
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.TickEvent
import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "NoWeb", spacedName = "No Web", description = "Prevents you from getting slowed down in webs.", category = ModuleCategory.MOVEMENT)
class NoWeb : Module() {

    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.nowebs", NoWebMode::class.java)
            .map{it.newInstance() as NoWebMode}
            .sortedBy{it.modeName}

    val mode: NoWebMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    private val modeValue: ListValue = object: ListValue("Mode", modes.map{ it.modeName }.toTypedArray(), "Vanilla") {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }
        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    override fun onInitialize() {
        modes.map { mode -> mode.values.forEach { value -> value.name = "${mode.modeName}-${value.name}" } }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mode.onUpdate()
    }

    fun onJump(event: JumpEvent) {
        mode.onJump(event)
    }

    fun onTick(event: TickEvent) {
        mode.onTick()
    }

    fun onMoveInput(event: MoveInputEvent) {
        mode.onMoveInput(event)
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0F
    }

    override val tag: String
        get() = modeValue.get()
}
