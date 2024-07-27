package net.minusmc.minusbounce.features.module.modules.movement.speeds

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.movement.Speed
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value


abstract class SpeedMode(val modeName: String, val typeName: SpeedType): MinecraftInstance() {
	protected val speed: Speed
		get() = MinusBounce.moduleManager[Speed::class.java]!!

	open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)

	open fun onEnable() {}

	open fun onDisable() {}

    open fun onUpdate() {}
    open fun onTick() {}
	open fun onPreMotion(event: PreMotionEvent) {}
	open fun onPostMotion(event: PostMotionEvent) {}
    open fun onReceivedPacket(event: ReceivedPacketEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStrafe(event: StrafeEvent) {}
    open fun onPreUpdate() {}
}
