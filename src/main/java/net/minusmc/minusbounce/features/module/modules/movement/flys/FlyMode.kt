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
    protected var startY = 0.0

	protected val fly: Fly
		get() = MinusBounce.moduleManager[Fly::class.java]!!

	open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)

    open fun initEnable() {
        mc.thePlayer ?: return
        startY = mc.thePlayer.posY
    }
    open fun resetMotion() {
        if (fly.resetMotionValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    open fun handleUpdate() {
        mc.thePlayer ?: return
        if (fly.fakeDmgValue.get()) mc.thePlayer.handleStatusUpdate(2.toByte())
    }

	open fun onEnable() {}
	open fun onDisable() {}
    open fun onPacket(event: PacketEvent) {}
    open fun onUpdate() {}
    open fun onMotion(event: MotionEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onRender2D() {}
    open fun onRender3D() {
        if (fly.markValue.get()) {
            val y = startY + 2
            val color = if (mc.thePlayer.entityBoundingBox!!.maxY < y) Color(0, 255, 0, 90) else Color(255, 0, 0, 90)
            RenderUtils.drawPlatform(y, color, 1.0)
        }
    }
    open fun onBlockBB(event: BlockBBEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
}
