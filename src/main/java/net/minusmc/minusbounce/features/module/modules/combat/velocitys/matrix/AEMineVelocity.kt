package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class AEMineVelocity : VelocityMode("AEMine") {
    override fun onUpdate() {
        if (mc.thePlayer.hurtTime <= 0) {
            return
        }
        if (mc.thePlayer.hurtTime >= 6) {
            mc.thePlayer.motionX *= 0.605001
            mc.thePlayer.motionZ *= 0.605001
            mc.thePlayer.motionY *= 0.727
        } else if (!mc.thePlayer.onGround) {
            mc.thePlayer.motionX *= 0.305001
            mc.thePlayer.motionZ *= 0.305001
            mc.thePlayer.motionY -= 0.095
        }
    }
}