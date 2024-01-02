package net.minusmc.minusbounce.features.module.modules.combat.velocitys.matrix


import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class MatrixVelocity : VelocityMode("Matrix") {

    override fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.hurtTime > 0) {
            mc.thePlayer.motionX *= 0.6
            mc.thePlayer.motionZ *= 0.6
        }
    }

}