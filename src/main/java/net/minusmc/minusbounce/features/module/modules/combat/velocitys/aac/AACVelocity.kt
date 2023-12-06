package net.minusmc.minusbounce.features.module.modules.combat.velocitys.aac


import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class AACVelocity : VelocityMode("AAC") {

    private var jump = false

    override fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.hurtTime > 0) {
            mc.thePlayer.motionX *= 0.6
            mc.thePlayer.motionZ *= 0.6
        }

        jump = false
    }

    override fun onMove() {
        if (jump) {
            mc.thePlayer.jump()
        }
    }

}