package net.minusmc.minusbounce.features.module.modules.world.scaffold

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.MinusBounce
import net.minecraft.stats.StatList

abstract class TowerScaffold(val modeName: String): MinecraftInstance() {

	protected val scaffold: Scaffold
		get() = MinusBounce.moduleManager[Scaffold::class.java]!!

	open fun onPreMotion(event: PreMotionEvent) {}

	open fun onPostMotion() {}

	protected fun fakeJump() {
        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }
}