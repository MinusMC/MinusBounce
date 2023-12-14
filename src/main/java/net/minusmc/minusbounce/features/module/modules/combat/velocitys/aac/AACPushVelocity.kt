package net.minusmc.minusbounce.features.module.modules.combat.velocitys.aac

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.features.module.modules.movement.Speed
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue


class AACPushVelocity : VelocityMode("AACPush") {
	private val aacPushXZReducerValue = FloatValue("XZReducer", 2F, 1F, 3F, "x")
    private val aacPushYReducerValue = BoolValue("YReducer", true)

	private var jump = false

	override fun onUpdate() {
		if (jump) {
            if (mc.thePlayer.onGround)
                jump = false
        } else {
            if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0)
                mc.thePlayer.onGround = true

            if (mc.thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get()
                    && !MinusBounce.moduleManager[Speed::class.java]!!.state)
                mc.thePlayer.motionY -= 0.014999993
        }

        if (mc.thePlayer.hurtResistantTime >= 19) {
            val reduce = aacPushXZReducerValue.get()

            mc.thePlayer.motionX /= reduce
            mc.thePlayer.motionZ /= reduce
        }
	}

	override fun onJump(event: JumpEvent) {
		jump = true
        if (!mc.thePlayer.isCollidedVertically)
            event.cancelEvent()
	}
}