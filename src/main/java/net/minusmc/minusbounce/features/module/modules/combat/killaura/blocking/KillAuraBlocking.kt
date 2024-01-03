package net.minusmc.minusbounce.features.module.modules.killaura.blocking

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.MinusBounce

abstract class KillAuraBlocking(val modeName: String): MinecraftInstance() {

	protected val killAura: KillAura
		get() = MinusBounce.moduleManager[KillAura::class.java]!!

	open fun onPreMotion() {}

	open fun onPostMotion() {}

	open fun onBeforeAttack() {}

	open fun onAfterAttack() {}

	open fun onPreUpdate() {}
}