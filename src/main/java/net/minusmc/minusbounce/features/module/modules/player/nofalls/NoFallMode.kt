package net.minusmc.minusbounce.features.module.modules.player.nofalls

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.player.NoFall
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value


abstract class NoFallMode(val modeName: String): MinecraftInstance() {
	protected val nofall: NoFall
		get() = MinusBounce.moduleManager[NoFall::class.java]!!

	open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)

	open fun onEnable() {}

	open fun onDisable() {}

    open fun onUpdate() {}
    open fun onPacket(event: PacketEvent) {}
    open fun onMotion(event: MotionEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onWorld(event: WorldEvent) {}
    open fun onJump() {}
	open fun onRender2D() {}
}
