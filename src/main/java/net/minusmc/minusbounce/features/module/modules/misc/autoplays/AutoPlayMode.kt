package net.minusmc.minusbounce.features.module.modules.misc.autoplays

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.misc.AutoPlay
import net.minusmc.minusbounce.features.module.modules.misc.AutoDisable
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.value.Value
import net.minusmc.minusbounce.utils.ClassUtils
import java.util.*
import kotlin.concurrent.schedule

abstract class AutoPlayMode(val modeName: String): MinecraftInstance() {
	protected var queued = false
	protected val autoplay: AutoPlay
		get() = MinusBounce.moduleManager[AutoPlay::class.java]!!


    open fun onEnable() {}
	open fun onWorld() {}
	open fun onPacket(event: PacketEvent) {}

	protected fun queueAutoPlay(delay: Long = autoplay.delayValue.get().toLong() * 1000, runnable: () -> Unit) {
        if (queued) return
        queued = true
        AutoDisable.handleGameEnd()
        if (autoplay.state) {
            Timer().schedule(delay) {
                queued = false
                if (autoplay.state) runnable()
            }
            MinusBounce.hud.addNotification(Notification("Sending you to a new game in ${delay}s!", Notification.Type.INFO, delay.toLong() * 1000L))
        }
    }

}