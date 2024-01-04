package net.minusmc.minusbounce.features.module.modules.killaura

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.MinusBounce

abstract class KillAuraBlocking(val modeName: String): MinecraftInstance() {

	protected val killAura: KillAura
		get() = MinusBounce.moduleManager[KillAura::class.java]!!
		
	open fun onPreMotion() {}

	open fun onPostMotion() {}

	open fun onPreAttack() {}

	open fun onPostAttack() {}

	open fun onPreUpdate() {}

	open fun onUpdate() {}

	open fun onPacket(event: PacketEvent) {}

	open fun onEnable() {}

	open fun onDisable() {}
}