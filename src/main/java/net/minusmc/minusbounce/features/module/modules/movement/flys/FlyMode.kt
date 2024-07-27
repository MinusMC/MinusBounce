package net.minusmc.minusbounce.features.module.modules.movement.flys

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.movement.Fly
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.Value
import java.awt.Color

abstract class FlyMode(val modeName: String, val typeName: FlyType): MinecraftInstance() {
    var startY = 0.0

	protected val fly: Fly
		get() = MinusBounce.moduleManager[Fly::class.java]!!

	open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)

	open fun onEnable() {
        mc.thePlayer ?: return
        startY = mc.thePlayer.posY
    }
    
	open fun onDisable() {}
    open fun onSentPacket(event: SentPacketEvent) {}
    open fun onReceivedPacket(event: ReceivedPacketEvent) {}
    open fun onUpdate() {}
    open fun onPreMotion(event: PreMotionEvent) {}
    open fun onPostMotion(event: PostMotionEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onRender2D() {}
    open fun onRender3D() {}
    open fun onBlockBB(event: BlockBBEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
}
