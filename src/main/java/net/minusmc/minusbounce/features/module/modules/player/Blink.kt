package net.minusmc.minusbounce.features.module.modules.player

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.player.BlinkUtils
import net.minusmc.minusbounce.value.*
import java.util.*

@ModuleInfo(name = "Blink", description = "Suspends all player packets.", category = ModuleCategory.PLAYER)
class Blink : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Sent", "Received", "Both"), "Both")
    private val pulseValue = BoolValue("PulseDelay", true)
    private val pulseDelayValue = IntegerValue("PulseDelay", 1000, 500, 5000, "ms") {pulseValue.get()}
    private val fakePlayerValue = BoolValue("FakePlayer", false)
    
    private val pulseTimer = MSTimer()

    override fun onEnable() {
        pulseTimer.reset()

        if (fakePlayerValue.get())
            BlinkUtils.addFakePlayer()
    }

    override fun onDisable() {
        mc.thePlayer ?: return

        BlinkUtils.unblink()
    }

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        when (modeValue.get().lowercase()) {
            "sent" -> BlinkUtils.blink(event, true, false)
            "both" -> BlinkUtils.blink(event)
        }
    }

    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
        when (modeValue.get().lowercase()) {
            "received" -> BlinkUtils.blink(event, false, true)
            "both" -> BlinkUtils.blink(event)
        }
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        mc.thePlayer ?: return

        if (mc.thePlayer.isDead || mc.thePlayer.ticksExisted <= 10)
            BlinkUtils.unblink()

        if (pulseValue.get() && pulseTimer.hasTimePassed(pulseDelayValue.get())) {
            BlinkUtils.unblink()

            if (fakePlayerValue.get())
                BlinkUtils.addFakePlayer()

            pulseTimer.reset()
        }
    }

    override val tag: String
        get() = BlinkUtils.packetsSize.toString()
}
