package net.minusmc.minusbounce.features.module.modules.movement.nowebs.other

import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode
import net.minusmc.minusbounce.utils.player.MovementUtils

class CardinalNoWeb: NoWebMode("Cardinal") {
    override fun onUpdate() {
        if (mc.thePlayer.onGround) MovementUtils.strafe(0.262F)
        else MovementUtils.strafe(0.366F)
    }
}