package net.minusmc.minusbounce.features.module.modules.movement.longjumps

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.movement.LongJump
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value

abstract class LongJumpMode(val modeName: String): MinecraftInstance() {
	protected val longjump: LongJump
		get() = MinusBounce.moduleManager[LongJump::class.java]!!

	open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)

	open fun onEnable() {}
	open fun onDisable() {}
    open fun onUpdate() {}
    open fun onUpdateJumped() {}
    open fun onSentPacket(event: SentPacketEvent) {}
    open fun onReceivedPacket(event: ReceivedPacketEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
}
