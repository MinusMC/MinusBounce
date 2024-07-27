package net.minusmc.minusbounce.features.module.modules.world.scaffold

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value

abstract class ModeScaffold(val modeName: String): MinecraftInstance() {

    protected val scaffold: Scaffold
		get() = MinusBounce.moduleManager[Scaffold::class.java]!!

    open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)


    open fun onEnable() {}
    open fun onDisable() {}
    open fun onUpdate() {}
    open fun onPreMotion(event: PreMotionEvent) {}
    open fun onPostMotion(event: PostMotionEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onJump(event: JumpEvent) {}

}