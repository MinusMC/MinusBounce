package net.minusmc.minusbounce.features.module.modules.movement.noslows

import net.minecraft.item.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.SlowDownEvent
import net.minusmc.minusbounce.features.module.modules.movement.NoSlow
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.Value

abstract class NoSlowMode(val modeName: String): MinecraftInstance() {
    protected val noslow: NoSlow
        get() = MinusBounce.moduleManager[NoSlow::class.java]!!

    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    open fun onEnable() {}

    open fun onDisable() {}
    open fun onUpdate() {}

    open fun onPreMotion(event: PreMotionEvent) {}
    open fun onPostMotion(event: PostMotionEvent) {}
}