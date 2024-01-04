package net.minusmc.minusbounce.features.module.modules.combat.killaura

import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.MinusBounce

abstract class KillAuraBlocking(val modeName: String): MinecraftInstance() {
	protected val killAura: KillAura
		get() = MinusBounce.moduleManager[KillAura::class.java]!!
		 
	protected var blockingStatus: Boolean
		get() = killAura.blockingStatus
		set(value: Boolean) {
			killAura.blockingStatus = value
		}

	open fun onPreMotion() {}

	open fun onPostMotion() {}

	open fun onPreAttack() {}

	open fun onPostAttack() {}

	open fun onPreUpdate() {}

	open fun onPacket(event: PacketEvent) {}

	open fun onDisable() {}
}