package net.minusmc.minusbounce.features.module.modules.player.antivoids

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.player.AntiVoid
import net.minusmc.minusbounce.utils.misc.FallingPlayer
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value

abstract class AntiVoidMode(val modeName: String): MinecraftInstance() {
	protected val antivoid: AntiVoid
		get() = MinusBounce.moduleManager[AntiVoid::class.java]!!

	open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)

	open fun onEnable() {}
	open fun onDisable() {}

    open fun onUpdate() {}
    open fun onPacket(event: PacketEvent) {}
    open fun onWorld() {}

    protected val isVoid: Boolean
    	get() = FallingPlayer(mc.thePlayer).findCollision(60) == null
}