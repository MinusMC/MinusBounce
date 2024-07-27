package net.minusmc.minusbounce.features.module.modules.combat.criticals

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.combat.Criticals
import net.minusmc.minusbounce.value.Value
import net.minusmc.minusbounce.utils.ClassUtils


abstract class CriticalMode(val modeName: String): MinecraftInstance() {
	protected val criticals: Criticals
		get() = MinusBounce.moduleManager[Criticals::class.java]!!

	open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)

	open fun onEnable() {}

	open fun onDisable() {}

    open fun onUpdate() {}
    open fun onSentPacket(event: SentPacketEvent) {}
    open fun onAttack(event: AttackEvent) {}
}
