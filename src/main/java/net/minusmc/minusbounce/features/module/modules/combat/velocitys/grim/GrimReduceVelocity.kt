package net.minusmc.minusbounce.features.module.modules.combat.velocitys.grim

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class GrimReduceVelocity : VelocityMode("GrimReduce") {
    override fun onUpdate() {
        if (mc.thePlayer.hurtTime > 0) {
            mc.thePlayer.motionX += -1.0E-7
            mc.thePlayer.motionY += -1.0E-7
            mc.thePlayer.motionZ += -1.0E-7
            mc.thePlayer.isAirBorne = true
        }
    }
}