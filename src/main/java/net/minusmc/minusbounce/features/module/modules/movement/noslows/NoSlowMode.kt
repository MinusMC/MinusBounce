package net.minusmc.minusbounce.features.module.modules.movement.noslows

import net.minecraft.item.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.SlowDownEvent
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.modules.movement.NoSlow
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.Value

abstract class NoSlowMode(val modeName: String): MinecraftInstance() {
    protected val msTimer = MSTimer()

    protected val noslow: NoSlow
        get() = MinusBounce.moduleManager[NoSlow::class.java]!!

    protected val killaura: KillAura
        get() = MinusBounce.moduleManager[KillAura::class.java]!!

    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    open fun onEnable() {
        msTimer.reset()
    }

    open fun onDisable() {}

    open fun onUpdate() {}

    open fun onPacket(event: PacketEvent) {}

    open fun onMotion(event: MotionEvent) {}

    open fun onSlowDown(event: SlowDownEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        val heldItem = mc.thePlayer.heldItem?.item
        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    open fun sendPacket(event: MotionEvent, sendC07: Boolean, sendC08: Boolean, delay: Boolean, delayValue: Long, onGround: Boolean, watchDog: Boolean = false) {
        if (onGround && !mc.thePlayer.onGround) return

        if (sendC07 && event.eventState == EventState.PRE && ((delay && msTimer.hasTimePassed(delayValue)) || !delay)) 
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1,-1,-1), EnumFacing.DOWN))

        if (sendC08 && event.eventState == EventState.POST) {
            if (delay && msTimer.hasTimePassed(delayValue) && !watchDog) {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
                msTimer.reset()
            } else if (!delay && !watchDog) mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
            else if (watchDog) mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f))
        }
    }

    open fun getMultiplier(item: Item?, isForward: Boolean) = when (item) {
        is ItemFood, is ItemPotion, is ItemBucketMilk -> {
            if (isForward) noslow.consumeForwardMultiplier.get() else noslow.consumeStrafeMultiplier.get()
        }
        is ItemSword -> {
            if (isForward) noslow.blockForwardMultiplier.get() else noslow.blockStrafeMultiplier.get()
        }
        is ItemBow -> {
            if (isForward) noslow.bowForwardMultiplier.get() else noslow.bowStrafeMultiplier.get()
        }
        else -> 0.2F
    }
}