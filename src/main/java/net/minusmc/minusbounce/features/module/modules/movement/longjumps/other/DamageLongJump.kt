package net.minusmc.minusbounce.features.module.modules.movement.longjumps.other

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.event.MoveEvent

class DamageLongJump : LongJumpMode("Damage") {
    private val damageBoostValue = FloatValue("Boost", 4.25F, 0F, 10F)
    private val damageHeightValue = FloatValue("Height", 0.42F, 0F, 10F)
    private val damageTimerValue = FloatValue("Timer", 1F, 0.05F, 10F)
    private val damageNoMoveValue = BoolValue("NoMove", false)
    private val damageARValue = BoolValue("AutoReset", false)

    private var damaged = false

    override fun onEnable() {
    	damaged = false
    }

	override fun onUpdate() {
		if (mc.thePlayer.hurtTime > 0 && !damaged) {
            damaged = true
            MovementUtils.strafe(damageBoostValue.get())
            mc.thePlayer.motionY = damageHeightValue.get().toDouble()
        }
        
        if (damaged) {
            mc.timer.timerSpeed = damageTimerValue.get()
            if (damageARValue.get() && mc.thePlayer.hurtTime <= 0)
                damaged = false
        }
	}

	override fun onMove(event: MoveEvent) {
		if (damageNoMoveValue.get() && !damaged)
            event.zeroXZ()
	}
}
