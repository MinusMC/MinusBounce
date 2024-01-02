package net.minusmc.minusbounce.features.module.modules.combat.velocitys

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.TickEvent
import net.minusmc.minusbounce.features.module.modules.combat.Velocity
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value


abstract class VelocityMode(val modeName: String): MinecraftInstance() {
	protected val velocity: Velocity
		get() = MinusBounce.moduleManager[Velocity::class.java]!!

	open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)

	open fun onEnable() {}

	open fun onDisable() {}

	open fun onMove() {}

    open fun onUpdate() {}
    open fun onPacket(event: PacketEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onMotion(event: MotionEvent) {}
	open fun onTick(event :TickEvent) {}
}
