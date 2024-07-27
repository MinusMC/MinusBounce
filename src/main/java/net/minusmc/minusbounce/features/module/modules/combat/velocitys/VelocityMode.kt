package net.minusmc.minusbounce.features.module.modules.combat.velocitys

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.event.TickEvent
import net.minusmc.minusbounce.event.EntityDamageEvent
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.KnockbackEvent
import net.minusmc.minusbounce.event.MoveInputEvent
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
    open fun onSentPacket(event: SentPacketEvent) {}
    open fun onReceivedPacket(event: ReceivedPacketEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onPreMotion(event: PreMotionEvent) {}
	open fun onTick() {}
	open fun onEntityDamage(event: EntityDamageEvent) {}
	open fun onMoveInput(event: MoveInputEvent) {}
	open fun onAttack(event: AttackEvent) {}
	open fun onKnockback(event: KnockbackEvent) {}
}
