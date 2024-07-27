package net.minusmc.minusbounce.features.module.modules.movement.longjumps.redesky

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import kotlin.math.max

class RedeskyLongJump : LongJumpMode("Redesky") {

	private val redeskyTimerBoostValue = BoolValue("TimerBoost", false)
    private val redeskyGlideAfterTicksValue = BoolValue("GlideAfterTicks", false)
    private val redeskyTickValue = IntegerValue("Ticks", 21, 1, 25)
    private val redeskyYMultiplier = FloatValue("YMultiplier", 0.77F, 0.1F, 1F)
    private val redeskyXZMultiplier = FloatValue("XZMultiplier", 0.9F, 0.1F, 1F)
    private val redeskyTimerBoostStartValue = FloatValue("TimerBoostStart", 1.85F, 0.05F, 10F) { redeskyTimerBoostValue.get() }
    private val redeskyTimerBoostEndValue = FloatValue("TimerBoostEnd", 1.0F, 0.05F, 10F) { redeskyTimerBoostValue.get() }
    private val redeskyTimerBoostSlowDownSpeedValue = IntegerValue("TimerBoost-SlowDownSpeed", 2, 1, 10) { redeskyTimerBoostValue.get() }

    private var currentTimer = 1f
    private var ticks = 0

    override fun onEnable() {
        ticks = 0
    	if (redeskyTimerBoostValue.get())
            currentTimer = redeskyTimerBoostStartValue.get()
    }

	override fun onUpdateJumped() {
		if (redeskyTimerBoostValue.get())
            mc.timer.timerSpeed = currentTimer

        if (ticks < redeskyTickValue.get()) {
            mc.thePlayer.jump()
            mc.thePlayer.motionY *= redeskyYMultiplier.get().toDouble()
            mc.thePlayer.motionX *= redeskyXZMultiplier.get().toDouble()
            mc.thePlayer.motionZ *= redeskyXZMultiplier.get().toDouble()
            ticks++
            return
        }

        if (redeskyGlideAfterTicksValue.get())
            mc.thePlayer.motionY += 0.03

        if (redeskyTimerBoostValue.get() && currentTimer > redeskyTimerBoostEndValue.get())
            currentTimer = max(0.08F, currentTimer - 0.05F * redeskyTimerBoostSlowDownSpeedValue.get())

        ticks++
	}
}
