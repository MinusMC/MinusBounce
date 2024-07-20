package net.minusmc.minusbounce.features.module.modules.movement.nowebs.other

import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.utils.player.MovementUtils

class HorizonNoWeb: NoWebMode("Horizon") {
    private val horizonSpeed = FloatValue("Speed", 0.1F, 0.01F, 0.8F)
    override fun onUpdate() {
        if (mc.thePlayer.isInWeb && mc.thePlayer.onGround) 
            MovementUtils.strafe(horizonSpeed.get())
    }
}